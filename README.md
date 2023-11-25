### Author : Sambhav Jain

### Email Id : sjain218@uic.edu

### Video Link: https://youtu.be/3F70ekzozD8?si=P7PlXatl2ZfhJmug

# Graph-Heist-using-NetGameSim

Welcome to `Graph-Heist-using-NetGameSim`! This project simulates a game involving a policeman and a thief navigating through a graph which are produced using NetGameSim. The application is built using Scala and Akka actors, with a focus on demonstrating actor model concepts and graph algorithms.
It's designed to demonstrate the power of the actor model in handling complex interactions within a graph-based game environment. The game is played on a perturbed graph, provided by NetGameSim. Players—either as a thief or police—navigate through the graph, with moves determined by confidence scores based on node and edge similarity scores.

<img src="https://drive.google.com/uc?export=view&id=1DbWaJdDOirIOg0fOO6Ct5OksXq6WatAI" width="500" alt="Police Thief Diagram">


### Game Rules:
1. Initially, both thief and police are placed at random nodes.
2. Players move alternately. The thief wins if they find a node with valuable data.
3. If both players land on the same node, the police catches the thief, and the thief loses.
4. If a player reaches a node with no exit paths, they lose, and the opponent wins automatically.

Players must restart the game after each round. The game showcases the efficacy of graph-based decision-making and the actor model in managing complex game dynamics.

## Features
- Graph-based game simulation
- Actor model implementation using Akka
- RESTful API for game interactions

### Endpoints:
This section describes the REST API endpoints available in the Policeman Thief Graph Game.
#### 1. `/move/thief`
- **Method**: GET
- **Description**: This endpoint moves the thief to the next node based on the game's logic. It updates the game state accordingly.

#### 2. `/move/police`
- **Method**: GET
- **Description**: Similar to the thief's move, this endpoint moves the police. It ensures the game's state is updated following the police's move.

#### 3. `/state`
- **Method**: GET
- **Description**: Retrieve the current state of the game. This includes the positions of both the thief and police, and any relevant game information.

#### 4. `/reset`
- **Method**: GET
- **Description**: Resets the game to its initial state. This is used to start a new game once the current game concludes.

# Componets and Modules

### 1. Graph Loading and Parsing
- Load and parse .ngs files using NetGameSim.
- Construct graphs using Google Guava library.

### 2. Graph Actor Creation
- `Main` object invokes `WebServerGame.start()` with paths to graph files.
- `ActorSystem` initialized and `GameActor` created for game logic.

### 3. Game Actor Logic
- `GameActor` handles game states, movements, and resets.
- Uses `GameLogic` for determining next moves based on confidence scores.

### 4. Game State Management
- `GameState` tracks positions, game over status, and results.

### 5. REST API Server Setup
- `WebServerGame` sets up an HTTP server with routes for game actions.
- Defines behavior of each route and maps to `GameActor` actions.

### 6. Game Interaction
- Players interact via REST API calls.
- Server processes requests and updates game state.

### 7. Automated Play Mode
- `automated-play` route in `WebServerGame` for automated play handled by `AutomatedGameClient`.

### 8. Server Execution and Shutdown
- Server starts on specified port with defined routes.
- Remains operational until stopped, then unbinds from port and terminates actor system.

### 9. Results Writing
- Used the `writeToLocalFile` and `writeToS3` function to output the final statistics and details of attacks to a specified file.

## Steps and Workflow
![Description](https://drive.google.com/uc?export=view&id=1caa-G6PM2einhkms5zCKSJNs2FF_bpVz)

## Usage

To use this project, you will need to have Akka-Http library, Scala, SBT installed and configured. Compile the project with SBT or your preferred Scala build tool, and run the application with the required command-line arguments.

## Project Structure

The project is structured as follows:

```plaintext
.
├── src/                          # Source files.
│   ├── main/
│   │   ├── scala/
│   │   │   ├── com/
│   │   │   │   └── example/
│   │   │   │       └── policemanthiefgame/
│   │   │   │           ├── actors/
│   │   │   │           │   └── GameActor.scala
│   │   │   │           ├── api/
│   │   │   │           │   └── WebServerGame.scala
│   │   │   │           ├── models/
│   │   │   │           │   └── GameState.scala
│   │   │   │           ├── services/
│   │   │   │           │   └── GameLogic.scala
│   │   │   │           ├── utils/
│   │   │   │           │   ├── GraphLoader.scala
│   │   │   │           │   └── NetGraph.scala
│   │   │   │           └── Main.scala
├── test/                         # Test files.
│   ├── scala/
│   │   └── com/
│   │       └── example/
│   │           └── policemanthiefgame/
│   │               ├── actors/
│   │               │   └── GameActorTest.scala
│   │               ├── api/
│   │               │   └── WebServerGameTest.scala
│   │               ├── models/
│   │               │   └── GameStateTest.scala
│   │               └── services/
│   │                   └── GameLogicTest.scala
├── build.sbt                     # SBT build configuration.
├── project/
│   ├── build.properties
│   └── plugins.sbt
├── .bsp/                         # BSP configuration files.
├── .idea/                        # IDEA configuration files.
├── inputs/                       # Input data for processing.
├── outputs/                      # Output data after processing.
└── target/                       # Compiled files.
    ├── classes/
    └── test-classes/
```



## How to Run the Project
You can execute the project either locally or on AWS EC2 instance.

### Locally

#### 1. Clone the GitHub Repository
`git clone https://github.com/sambhav07/Graph-Heist-using-NetGameSim.git`<br>
`cd Graph-Heist-using-NetGameSim/`

#### 2. Build the Project
`sbt clean compile/`<br>
This ensures that all the dependencies from build.sbt are resolved and indexed locally.

#### 3. Set Program Arguments
- Go to edit configurations and on the top left corner click on `+` button and which will prompt a dialogue box to add new configuration
- Select Application and then give the name to the configuration, provide the module name along with the main class which is your entry point for the execution of the project
- Pass the program arguments from edit configurations as below:<br>
`"/Users/sambhavjain/Desktop/newrepo/Policeman_Thief_Graph_Game/inputs/NetGameSimNetGraph_21-11-23-20-09-22.ngs"
"/Users/sambhavjain/Desktop/newrepo/Policeman_Thief_Graph_Game/inputs/NetGameSimNetGraph_21-11-23-20-09-22.ngs.perturbed"
"/Users/sambhavjain/Desktop/newrepo/Policeman_Thief_Graph_Game/outputs/report.txt"`<br>

or alternatively you can also run using the command sbt run in the following way:
- Invoke the command from the terminal passing the required arguments: `sbt "run https://policethiefgame.s3.amazonaws.com/inputs/NetGameSimNetGraph_21-11-23-20-09-22.ngs https://policethiefgame.s3.amazonaws.com/inputs/NetGameSimNetGraph_21-11-23-20-09-22.ngs.perturbed https://policethiefgame.s3.amazonaws.com/output/ "`
##### Note: The above files should be present in your local or s3 bucket system and hence give the path accordingly

#### 4. Run the Main File
Execute Main to start the project. The final output files can be found at -:<br> `/Users/sambhavjain/Desktop/newrepo/Policeman_Thief_Graph_Game/outputs/report.txt`.

#### 5. Run the Tests
To ensure the functionality and correctness of the implemented logic, execute the test suites available in the project using command `sbt test` . Test results will be displayed in the terminal, showcasing passed and failed tests.

### AWS EC2 Instance
For Initial Setup
Create an AWS S3 bucket to host the JAR and necessary input files (i.e., .ngs files).
Inside the bucket, create input, jar, and output folders.
Running Steps
#### 1. Build the JAR
`sbt clean compile assembly`<br>

#### 2. Deploy the JAR to S3
Upload the JAR into the jar folder in the S3 bucket.

#### 3. Place Input Files on S3
Add the .ngs files for the original and perturbed graphs.

#### 4. Setup the EC2 Instance
After you setup the instance pull the jar from the github repo using the following command:
`wget https://policethiefgame.s3.amazonaws.com/jar/police_thief_game.jar -P /home/ubuntu/myapp`

And then invoke the jar using the following command:

`java -jar police_thief_game.jar https://policethiefgame.s3.amazonaws.com/inputs/NetGameSimNetGraph_21-11-23-20-09-22.ngs https://policethiefgame.s3.amazonaws.com/inputs/NetGameSimNetGraph_21-11-23-20-09-22.ngs.perturbed https://policethiefgame.s3.amazonaws.com/output/`

#### 5. View the Results
After the automated endpoint is invoked and  completed, results can be viewed in the output directory in the S3 bucket or in the same ec2 instance inside myapp directory with the file name as report.txt.

## Output:
Below are some glimpses for the responses produced by a game on a set of 21 nodes in the original and perturbed graph :

<p align="center">
  <img src="https://drive.google.com/uc?export=view&id=1YebMxuIk4S1eXoAQ4ZBYRlBYvi_mWNRp" width="800" alt="Description"/>
  <img src="https://drive.google.com/uc?export=view&id=1_y5-8ym1LrFTY7gmr8ZLYdOOUBha9dTn" width="800" alt="Description"/>
</p>
<p align="center">
  <img src="https://drive.google.com/uc?export=view&id=13-ll1qY2zqkLrWU_-vj3RaIqUcdjkQop" width="800" alt="Description"/>
  <img src="https://drive.google.com/uc?export=view&id=1vhBiSljz-doSIdui_qaVFrBnpw0UXmip" width="800" alt="Description"/>
</p>


## Sample Graph:
![Description](https://drive.google.com/uc?export=view&id=1K3UKD25r3Wh8aQlwIFkrf6qHkxZ8lx7K)

## Technologies Used

- **Scala:** A high-level, general-purpose programming language.
- **Akka HTTP:** Toolkit for building REST/HTTP-based integration layers on top of Scala and Akka.
- **Akka Actors:** Toolkit for building highly concurrent, distributed, and resilient message-driven applications in Scala.
- **Google Guava:** A set of core libraries for Java, mainly used here for its graph data structures.
