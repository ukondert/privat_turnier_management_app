PRAGMA foreign_keys = OFF;
DELETE FROM tournament_player;
DELETE FROM match;
DELETE FROM round;
DELETE FROM tournament;
DELETE FROM player;
PRAGMA foreign_keys = ON;
