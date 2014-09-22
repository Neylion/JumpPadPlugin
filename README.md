# JumpPadPlugin

A JumpPad plugin for Minecraft

## Release history

### JumpStraightUp

* CHANGE: The player now jumps straight up, before being sent off with forward velocity.

### develop

* BUG: The player was not nudged off the ground before the jump
* CHANGE: The command edit now requires the player to be on a jumppad first.

### 1.0.1 (2014-09-16)

* NEW: Nudges the user up in the air before sending him away, to avoid speed differences due to friction differens beteween air and ground.
* CHANGE: Shorter usage text, showing detailed usage text only if a sub command is issued
* CHANGE: Now stores the jump pad locations as the block location, not the creators location.

### 1.0 (2014-09-16)

* The /jumppad command supports add, remove, edit, goto and list