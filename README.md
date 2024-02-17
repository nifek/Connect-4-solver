# Connect-4-solver
A strong solver of Connect 4 implemented in Java 

Available commands:

exit / stop: terminates the program.

UI [n] [m] [mover (0 / 1)]: opens a user interface with the ability to play an n by m Connect-4 game

solve-position [n] [m] [weak (0 / 1)] [position]: solves the given position of a n by m Connect-4 game (outputs strong solution if weak = 0, weak solution otherwise).

play [n] [m] [output_score (0 / 1)]: allows a user to play a n by m Connect-4 game with himself. Also outputs a strong score of a position of output_score is not 0.

run-test-group [group_game ({easy, medium, hard})]: runs a Connect-4 solver on a chosen test group and outputs its peformance.

show-perfect-game [n] [m]: calculates and outputs a perfect n by m Connect-4 game.

create-table [n] [m] [depth]: calculates the scores and best moves of all different positions of n by m Connect-4 to a desired depth. Stores the results in a file named "n-m" (where n and m are from the input).

1v1 [n] [m] [mover (0 / 1)]: plays a n by m Connect-4 game against the solver, where user is the "mover" player (0 - first, 1 - second).
