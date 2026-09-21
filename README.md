# ChainDB — Event-Sourced Database Engine with Blockchain Integrity

ChainDB is a lightweight, SQL-like, in-memory database engine written in
**pure Core Java (17+)** with **zero external dependencies** — no Spring, no
Hibernate, no JDBC/real databases, no blockchain libraries.

Unlike a traditional database, ChainDB never overwrites a row in place.
Every `CREATE TABLE` / `INSERT` / `UPDATE` / `DELETE` you run is captured as an
immutable **event**, wrapped in a **mined, hash-linked block**, and appended
to a blockchain. The "database" you query is not stored anywhere directly —
it is rebuilt from scratch every time the app starts by **replaying every
block's event, in order, from genesis**. If anyone edits a historical
record, the recalculated hash won't match what's stored, and ChainDB tells
you exactly which block was tampered with.

---

## 1. Features

- SQL-like console: `CREATE TABLE`, `INSERT`, `UPDATE`, `DELETE`, `SELECT * [WHERE]`, `SHOW TABLES`, `DESCRIBE`
- Full event sourcing: every mutation is a `DatabaseEvent` object that knows how to replay itself
- Real blockchain mechanics: SHA-256 hashing, previous-hash linking, proof-of-work mining, full-chain verification
- Tamper detection: `VERIFYCHAIN` / `REPORT` recompute every hash and flag exactly which block(s) were altered
- Single-file persistence: the entire application state lives in one `blockchain.dat` (Java serialization) — restart and your data (and your history) comes back
- **Reports**: table/row counts, block counts, per-operation-type counts, blockchain validity
- **Blockchain Explorer**: list all blocks, inspect one in full detail, or search events by keyword
- **JSON export** of the whole chain (hand-built, no JSON library)
- **UNDO** via compensating events (an INSERT is undone by a new DELETE block, a DELETE by a new INSERT block, etc.) — history is never rewritten, only extended
- Dynamic column type inference (`INT` / `DOUBLE` / `STRING`), shown via `DESCRIBE`
- Custom checked exceptions, clean layered architecture, Command / Factory / Singleton / Repository patterns

## 2. Architecture (ASCII)

```
+-------------------------------------------------------------------------+
|                              chaindb.ui                                 |
|   Main  ->  ConsoleUI  (reads input, prints tables/messages/reports)     |
+-------------------------------------------------------------------------+
                 |                     |                     |
                 v                     v                     v
      chaindb.parser          chaindb.engine          chaindb.service
   SqlParser -> ParsedCommand   DatabaseEngine           ReportService
        (regex grammar)         (Singleton)            ExplorerService
                                QueryExecutor           JsonExportService
                              (Command dispatch)          UndoService
                                     |
                 +-------------------+-------------------+
                 v                                        v
        chaindb.event                             chaindb.blockchain
   DatabaseEvent (abstract)                     Blockchain (chain of Block)
   CreateTableEvent / InsertEvent /                     Block
   UpdateEvent / DeleteEvent                     (SHA-256, mining, verify)
   EventFactory (Factory Pattern)                        |
                 |                                       v
                 v                             chaindb.storage
        chaindb.model                     BlockchainRepository (interface)
   Database -> Table -> Column / Row      FileBlockchainRepository (Repo Pattern)
   (pure in-memory, no DB engine)               -> blockchain.dat (only file)
                 ^
                 |
      reconstructed ONLY by replaying every
      Block's DatabaseEvent.apply(database)
      in order, starting from an empty Database
+-------------------------------------------------------------------------+
             chaindb.exception (cross-cutting custom checked exceptions)
   InvalidQueryException, TableNotFoundException,
   ColumnNotFoundException, BlockchainException
+-------------------------------------------------------------------------+
```

## 3. Class Diagram (ASCII)

```
DatabaseEvent (abstract)                    Block
  # timestamp : long                          - blockNumber : int
  # tableName : String                        - timestamp : long
  # operation : String                        - event : DatabaseEvent
  + apply(Database) : void  {abstract}        - previousHash : String
  + getPayload() : String   {abstract}        - currentHash : String
        ^      ^      ^      ^                - nonce : long
        |      |      |      |                + calculateHash() : String
   CreateTable  Insert  Update  Delete         + mineBlock(difficulty) : void
   Event        Event   Event   Event
                                             Blockchain
Database                                      - chain : List<Block>
  - name : String                             - difficulty : int
  - tables : Map<String,Table>                + appendBlock(DatabaseEvent) : Block
  + addTable / getTable / hasTable            + verifyChain() : List<String>
                                               + isValid() : boolean
Table
  - name : String                           BlockchainRepository <<interface>>
  - columns : Map<String,Column>              + save(Blockchain)
  - rows : List<Row>                          + load() : Blockchain
  + insertRow / updateRows / deleteRows              ^
  + selectWhere                                      |
                                             FileBlockchainRepository
Column                                         -> blockchain.dat
  - name : String
  - type : DataType {ANY,INT,DOUBLE,STRING} DatabaseEngine  <<singleton>>
                                               - database : Database
Row                                           - blockchain : Blockchain
  - values : LinkedHashMap<String,Object>     - repository : BlockchainRepository
                                               + commitEvent(DatabaseEvent) : Block
SqlParser                 EventFactory           (apply -> mine -> persist)
  + parse(String)           <<factory>>
    : ParsedCommand         + createEvent(ParsedCommand, Database)
                              : DatabaseEvent

QueryExecutor                              CommandHandler <<interface>>
  - handlers : Map<CommandType,               + handle(ParsedCommand)
      CommandHandler>       <<command pattern>>  : QueryResult
  + execute(ParsedCommand) : QueryResult
```

## 4. Flow Diagram — executing `INSERT INTO Student VALUES(101,"Alice",8.4)`

```
 User types command
        |
        v
 ConsoleUI.start()  --------->  SqlParser.parse(line)
        |                              |
        |                     matches INSERT pattern
        |                              |
        |                     ParsedCommand{type=INSERT, table=Student,
        |                                   values={__pos0:101, __pos1:"Alice", __pos2:8.4}}
        v
 QueryExecutor.execute(cmd)
        |
        v
 EventFactory.createEvent(cmd, database)
        |   looks up Table "Student" columns (id,name,cgpa)
        |   maps positional values -> {id:101, name:"Alice", cgpa:8.4}
        v
 InsertEvent(tableName="Student", values={...})
        |
        v
 DatabaseEngine.commitEvent(event)
        |-- 1. event.apply(database)  => Table.insertRow(new Row(values))
        |-- 2. blockchain.appendBlock(event)
        |         -> new Block(n, event, previousHash)
        |         -> block.mineBlock(difficulty)  [SHA-256 PoW, "0000" prefix]
        |-- 3. repository.save(blockchain)  => blockchain.dat (Java serialization)
        v
 QueryResult.message("1 row inserted into 'Student'.")
        |
        v
 ConsoleUI prints the result to the terminal
```

## 5. Flow Diagram — startup (state reconstruction)

```
 Main.main()
     |
     v
 DatabaseEngine.getInstance()  (Singleton, first call)
     |
     |-- FileBlockchainRepository.load()
     |       reads blockchain.dat via ObjectInputStream
     |       returns existing Blockchain, or null if file absent
     |
     |-- if null: new Blockchain()  (mines a Genesis Block)
     |
     |-- new Database("ChainDB")   (starts empty)
     |
     '-- replayEvents():
             for each Block in blockchain.getChain():
                 if block.getEvent() != null:
                     block.getEvent().apply(database)
             => Database now reflects every historical
                CREATE_TABLE / INSERT / UPDATE / DELETE,
                in the exact order they originally happened
```

## 6. Design Patterns Used

| Pattern    | Where                                                             |
|------------|--------------------------------------------------------------------|
| Singleton  | `DatabaseEngine` — one engine, one blockchain, one database per run |
| Factory    | `EventFactory` — builds the right `DatabaseEvent` subtype from a `ParsedCommand` |
| Command    | `QueryExecutor` + `CommandHandler` — each `CommandType` is dispatched to an isolated handler |
| Repository | `BlockchainRepository` / `FileBlockchainRepository` — abstracts persistence away from the engine |

## 7. Project Structure

```
ChainDB/
├── README.md
└── src/
    └── chaindb/
        ├── model/       Database, Table, Column, Row, DataType
        ├── event/       DatabaseEvent, CreateTableEvent, InsertEvent,
        │                UpdateEvent, DeleteEvent, EventFactory
        ├── blockchain/  Block, Blockchain
        ├── engine/      DatabaseEngine, QueryExecutor, CommandHandler, QueryResult
        ├── parser/      SqlParser, ParsedCommand, CommandType
        ├── storage/     BlockchainRepository, FileBlockchainRepository
        ├── service/     ReportService, ExplorerService, JsonExportService, UndoService
        ├── util/        HashUtil, ValueParser, ConsoleTablePrinter
        ├── ui/          ConsoleUI, Main
        └── exception/   InvalidQueryException, TableNotFoundException,
                         ColumnNotFoundException, BlockchainException
```

## 8. Installation & Running

Requires **JDK 17 or newer**. No build tool, no third-party jars.

### Command line
```bash
cd ChainDB
mkdir out
javac -d out $(find src -name "*.java")
cd out
java chaindb.ui.Main
```

### IntelliJ IDEA
1. Open the `ChainDB` folder as a project (File → Open).
2. When prompted, mark `src` as **Sources Root** (right-click `src` → *Mark Directory as* → *Sources Root*).
3. Set the Project SDK to Java 17+ (File → Project Structure → Project).
4. Run `chaindb.ui.Main`.

`blockchain.dat` is created in the working directory the first time you run
a mutating command, and is loaded automatically on every subsequent run —
this file *is* your database.

## 9. Usage — Sample Session

```
ChainDB > CREATE TABLE Student(id,name,cgpa)
Table 'Student' created.
ChainDB > INSERT INTO Student VALUES(101,"Alice",8.4)
1 row inserted into 'Student'.
ChainDB > INSERT INTO Student VALUES(102,"Bob",7.9)
1 row inserted into 'Student'.
ChainDB > SELECT * FROM Student
+-----+-------+------+
| id  | name  | cgpa |
+-----+-------+------+
| 101 | Alice | 8.4  |
| 102 | Bob   | 7.9  |
+-----+-------+------+
2 row(s) returned.
ChainDB > UPDATE Student SET cgpa=9.2 WHERE id=101
Row(s) updated in 'Student'.
ChainDB > SELECT * FROM Student WHERE id=101
+-----+-------+------+
| id  | name  | cgpa |
+-----+-------+------+
| 101 | Alice | 9.2  |
+-----+-------+------+
1 row(s) returned.
ChainDB > SHOW TABLES
Tables:
  - Student
ChainDB > DESCRIBE Student
Table: Student
  id : INT
  name : STRING
  cgpa : DOUBLE
ChainDB > REPORT
========== ChainDB Report ==========
Total Tables:            1
Total Rows:               2
Total Blocks:             5
Total CREATE_TABLE ops:   1
Total INSERT operations:  2
Total UPDATE operations:  1
Total DELETE operations:  0
Blockchain Valid:         YES
=====================================
ChainDB > VERIFYCHAIN
Blockchain integrity check: VALID. All 4 block(s) verified successfully.
ChainDB > DELETE FROM Student WHERE id=102
Row(s) deleted from 'Student'.
ChainDB > UNDO
Undo applied: compensating INSERT(s) for last DELETE on 'Student'.
ChainDB > EXIT
Persisting blockchain and shutting down...
Goodbye!
```

This exact session was run against the compiled jar to produce this
transcript (see `sample_execution.txt` for the full raw console output,
including `EXPLORER` and `EXPORT JSON`).

## 10. Sample Blockchain

`sample_blockchain_export.json` (included) is a real JSON export produced by
`EXPORT JSON` after running the session above — open it to see the actual
`blockNumber`, `timestamp`, `previousHash`, `currentHash`, `nonce`, and
`event` fields for every block, genesis included.

## 11. Tamper Detection, Demonstrated

If a byte of historical event data is changed directly in `blockchain.dat`
(bypassing the app entirely — e.g. with a hex editor), the database will
still **replay** using the corrupted value (event sourcing has no way to
know a historical fact is "wrong" on its own) — but `VERIFYCHAIN` and
`REPORT` will immediately flag it:

```
ChainDB > VERIFYCHAIN
Blockchain integrity check: INVALID.
  ! Block #2: stored hash does not match recalculated hash (data tampering detected)
```

This is the entire point of ChainDB: **the data can be edited, but the edit
cannot be hidden.**

## 12. Future Improvements

- Periodic snapshots every N blocks to speed up startup on very long chains (avoid full replay)
- Range/comparison operators in `WHERE` (`>`, `<`, `!=`) beyond equality
- Multi-column composite `WHERE` clauses (`AND` / `OR`)
- A pluggable `BlockchainRepository` backed by an append-only log file instead of whole-object serialization, to avoid rewriting the entire file on every commit
- Concurrent/multi-user access with proper locking
- A REST or gRPC front-end alongside the console UI
