# ChainDB

### Event-Sourced Database Engine with Blockchain Integrity

ChainDB is a lightweight, SQL-like database engine built from scratch using **Core Java 17+**. It combines **event sourcing** with a **hash-linked blockchain** to maintain an auditable history of database operations.

Instead of directly overwriting database records, ChainDB records database mutations such as `CREATE TABLE`, `INSERT`, `UPDATE`, and `DELETE` as events. Each event is stored inside a mined blockchain block and linked to the previous block using cryptographic hashes.

When the application starts, the database state is reconstructed by replaying the historical events stored in the blockchain.

---

## 🚀 Features

\- SQL-like interactive console

\- `CREATE TABLE`

\- `INSERT`

\- `UPDATE`

\- `DELETE`

\- `SELECT`

\- `SHOW TABLES`

\- `DESCRIBE`

\- Event-sourced database operations

\- SHA-256 hashing

\- Hash-linked blockchain

\- Proof-of-work block mining

\- Blockchain integrity verification

\- Tamper detection

\- Persistent blockchain storage

\- Blockchain Explorer

\- Database reports

\- JSON blockchain export

\- `UNDO` using compensating events

\- Dynamic column type inference

\- Custom checked exceptions

\- Layered Java architecture

\- Singleton, Factory, Command, and Repository design patterns

---

## 🛠️ Technology Stack

| Technology | Usage |
|---|---|
| Java 17+ | Core implementation |
| Object-Oriented Programming | Application architecture |
| SHA-256 | Block hashing and integrity verification |
| Java Serialization | Blockchain persistence |
| Regular Expressions | SQL-like command parsing |
| Java Collections Framework | Database and blockchain data structures |

**External dependencies:** None

ChainDB is implemented using Core Java without Spring, Hibernate, JDBC, an external database, or a blockchain library.

## Architecture

```text
                         ┌───────────────────────┐
                         │       Console UI      │
                         │       ConsoleUI       │
                         └───────────┬───────────┘
                                     │
                                     ▼
                         ┌───────────────────────┐
                         │      SQL Parser       │
                         │      SqlParser        │
                         └───────────┬───────────┘
                                     │
                                     ▼
                         ┌───────────────────────┐
                         │    Query Executor     │
                         │    Command Handler    │
                         └───────────┬───────────┘
                                     │
                                     ▼
                         ┌───────────────────────┐
                         │    Event Factory      │
                         │    Database Events    │
                         └───────────┬───────────┘
                                     │
                    ┌────────────────┴────────────────┐
                    ▼                                 ▼
          ┌──────────────────┐              ┌──────────────────┐
          │   In-Memory DB   │              │    Blockchain    │
          │ Database         │              │ Block/Blockchain │
          │ Table            │              │ SHA-256 + PoW    │
          │ Row / Column     │              │ Integrity Check  │
          └──────────────────┘              └────────┬─────────┘
                                                     │
                                                     ▼
                                           ┌──────────────────┐
                                           │    Repository    │
                                           │ FileBlockchain   │
                                           │ Repository       │
                                           └────────┬─────────┘
                                                    │
                                                    ▼
                                           ┌──────────────────┐
                                           │  blockchain.dat  │
                                           └──────────────────┘
## How ChainDB Works
For example, when a user executes:
INSERT INTO Student VALUES (101, 'Ramu', 8.75);
the command follows this flow:
User Command
     │
     ▼
ConsoleUI
     │
     ▼
SqlParser
     │
     ▼
ParsedCommand
     │
     ▼
QueryExecutor
     │
     ▼
EventFactory
     │
     ▼
InsertEvent
     │
     ├──────────────► Update In-Memory Database
     │
     ▼
DatabaseEngine
     │
     ▼
Create Blockchain Block
     │
     ▼
SHA-256 Hash + Proof of Work
     │
     ▼
Append Block to Blockchain
     │
     ▼
Persist Blockchain
     │
     ▼
blockchain.dat
## Event Sourcing + Blockchain
ChainDB records database mutations as events rather than directly storing only the latest state.
CREATE TABLE
      │
      ▼
CreateTableEvent
      │
      ▼
   Block #1
      │
      ▼
   Hash #1
      │
      ▼
INSERT
      │
      ▼
InsertEvent
      │
      ▼
   Block #2
      │
      ▼
   Hash #2
      │
      ▼
UPDATE
      │
      ▼
UpdateEvent
      │
      ▼
   Block #3
Each block stores the hash of the previous block, creating a linked chain of historical database operations.
When ChainDB starts, it replays the stored events in order to reconstruct the current database state.
## Blockchain Integrity Verification
ChainDB provides:
VERIFYCHAIN
to verify the integrity of the blockchain.
The verification process checks the stored hashes and the relationships between blocks.
Example:
ChainDB > VERIFYCHAIN
Blockchain integrity check: VALID.
All blocks verified successfully.
If historical blockchain data is modified, the recalculated hash can differ from the stored hash, allowing ChainDB to detect the modification.
## Blockchain Explorer
ChainDB includes an interactive blockchain explorer:
ChainDB > EXPLORER
Example execution:
--- Blockchain Explorer ---
#0 GENESIS
#1 CREATE_TABLE Student
#2 CREATE_TABLE Employee
#3 INSERT Employee
#4 INSERT Employee
#5 INSERT Employee
#6 UPDATE Employee
#7 DELETE Employee
The explorer allows users to inspect the blockchain and search or inspect events.
## Database Reports
The REPORT command provides information about the database and blockchain.
Example from a verified ChainDB execution:
========== ChainDB Report ==========
Total Tables:            2
Total Rows:              2
Total Blocks:            8
Total CREATE_TABLE ops:  2
Total INSERT operations: 3
Total UPDATE operations: 1
Total DELETE operations: 1
Blockchain Valid:        YES
=====================================
## JSON Export
The complete blockchain can be exported using:
EXPORT JSON
Example:
ChainDB > EXPORT JSON
Blockchain exported to blockchain_export.json
A sample blockchain export is included in the repository.
## Persistence
ChainDB uses a file-based repository for blockchain persistence.
FileBlockchainRepository
          │
          ▼
    blockchain.dat
When ChainDB starts:
blockchain.dat
      │
      ▼
Load Blockchain
      │
      ▼
Replay Events
      │
      ▼
Reconstruct Database
This allows database state and historical events to persist across application restarts.
## Design Patterns
Pattern	Implementation	Purpose
Singleton	DatabaseEngine	Maintains a single database engine instance
Factory	EventFactory	Creates the appropriate database event
Command	QueryExecutor / CommandHandler	Dispatches commands to handlers
Repository	BlockchainRepository / FileBlockchainRepository	Separates persistence from application logic
## Project Structure
ChainDB/
│
├── README.md
├── sample_blockchain_export.json
├── sample_execution.txt
│
└── src/
    └── chaindb/
        │
        ├── blockchain/
        │   ├── Block.java
        │   └── Blockchain.java
        │
        ├── engine/
        │   ├── CommandHandler.java
        │   ├── DatabaseEngine.java
        │   ├── QueryExecutor.java
        │   └── QueryResult.java
        │
        ├── event/
        │   ├── DatabaseEvent.java
        │   ├── CreateTableEvent.java
        │   ├── InsertEvent.java
        │   ├── UpdateEvent.java
        │   ├── DeleteEvent.java
        │   └── EventFactory.java
        │
        ├── exception/
        │   ├── BlockchainException.java
        │   ├── ColumnNotFoundException.java
        │   ├── InvalidQueryException.java
        │   └── TableNotFoundException.java
        │
        ├── model/
        │   ├── Database.java
        │   ├── Table.java
        │   ├── Column.java
        │   ├── Row.java
        │   └── DataType.java
        │
        ├── parser/
        │   ├── SqlParser.java
        │   ├── ParsedCommand.java
        │   └── CommandType.java
        │
        ├── service/
        │   ├── ExplorerService.java
        │   ├── JsonExportService.java
        │   ├── ReportService.java
        │   └── UndoService.java
        │
        ├── storage/
        │   ├── BlockchainRepository.java
        │   └── FileBlockchainRepository.java
        │
        ├── ui/
        │   ├── ConsoleUI.java
        │   └── Main.java
        │
        └── util/
            ├── ConsoleTablePrinter.java
            ├── HashUtil.java
            └── ValueParser.java
## Running ChainDB
Requirements
JDK 17 or newer
Git (optional)
No external libraries required
Check your Java installation:
java -version
javac -version
Windows PowerShell
Open PowerShell inside the ChainDB directory.
1\. Compile the project
mkdir out
javac -d out (Get-ChildItem -Recurse -Filter *.java src).FullName
2\. Run ChainDB
java -cp out chaindb.ui.Main
You should see:
=====================================================
  ChainDB - Event-Sourced Database Engine
  with Blockchain Integrity Verification
=====================================================
Type HELP for a list of commands.
ChainDB >
## Example Usage
CREATE TABLE Student (id INT, name STRING, cgpa DOUBLE);
INSERT INTO Student VALUES (101, 'Ramu', 8.75);
INSERT INTO Student VALUES (102, 'Priya', 8.90);
SELECT * FROM Student;
UPDATE Student SET cgpa = 9.10 WHERE id = 101;
SELECT * FROM Student;
DELETE FROM Student WHERE id = 102;
VERIFYCHAIN
REPORT
EXPLORER
EXPORT JSON
## Verified Execution
The current implementation has been successfully tested through the interactive console with:
CREATE TABLE
INSERT
SELECT
UPDATE
DELETE
VERIFYCHAIN
EXPLORER
REPORT
EXPORT JSON
A verified execution produced:
Total Tables:            2
Total Rows:              2
Total Blocks:            8
Blockchain Valid:        YES
The blockchain explorer showed:
#0 GENESIS
#1 CREATE_TABLE Student
#2 CREATE_TABLE Employee
#3 INSERT Employee
#4 INSERT Employee
#5 INSERT Employee
#6 UPDATE Employee
#7 DELETE Employee
## Learning Outcomes
This project demonstrates practical experience with:
Core Java
Object-Oriented Programming
Java Collections
Exception Handling
File Handling
SQL-like command parsing
Event-driven architecture
Event sourcing
Blockchain fundamentals
SHA-256 hashing
Proof of Work
Data persistence
Design Patterns
Software architecture
## Future Improvements
Possible future improvements include:
Database snapshots for faster startup
Additional WHERE operators such as >, <, and !=
Composite WHERE conditions
Append-only log-based persistence
Concurrent and multi-user access
REST or gRPC interface
Expanded query support
Automated unit and integration tests
## Author
Ramu Akunuri
B.Tech Information Technology
GitHub: akunuriramu
