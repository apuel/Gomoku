# Gomoku

Gomoku is a 42 project where you have to design the game and the AI for users to play against. It must allow users to play another user, or face the AI. We are expected to design our own heuristic for the AI and to have it win within X amount of moves against players.

This variant has a few rules that we must follow.
--• Capture (As in the Ninuki-renju or Pente variants) : You can remove a pair of your
opponent’s stones from the board by flanking them with your own stones (See the
appendix). This rule adds a win condition : If you manage to capture ten of your
opponent’s stones, you win the game.
--• Game-ending capture : A player that manages to align five stones only wins if the
opponent can not break this alignment by capturing a pair, or if he has already lost
four pairs and the opponent can capture one more, therefore winning by capture.
There is no need for the game to go on if there is no possibility of this happening.
--• No double-threes : It is forbidden to play a move that introduces two free-three
alignments, which would guarantee a win by alignment (See the appendix)

The AI must also abide by these rules. A few rules were also put in place for how the AI must be designed. It needs to generate possible-solution trees and choose the best move according to this based off what heuristics we design. We have to use a Min-Max algorithm for this. 

A GUI is required as well for users to be able to play Gomoku. We are required to have a timer that counts how long it takes for our AI to move. This is a requirement or the project is considered a failure.
