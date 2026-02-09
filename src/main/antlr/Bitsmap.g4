grammar Bitsmap;
file: setStmt+ bitStmt* EOF;

// Set statement
setStmt: 'set' MAP fixture;

// Bit statement
bitStmt: mappedMovement (',' mappedMovement)* '{' bitFields+ '}';
bitFields: flowField | animField | rotateField | moveField | wiggleMulField;

// Bit fields
flowField: 'flow' EASING? DECIMAL;
animField: 'anim' STRING;
rotateField: 'rotate' bone vec3i;
moveField: 'move' bone vec3i;
wiggleMulField: 'wigglemul' DECIMAL;

// Thingies
mappedMovement: MAP movement;
vec3i: iaxisX? iaxisY? iaxisZ?;
iaxisX: 'x' ':' DECIMAL;
iaxisY: 'y' ':' DECIMAL;
iaxisZ: 'z' ':' DECIMAL;
bone: STRING;
movement: ID;
fixture: ID;

// Lexer (NOTE: ORDER MATTERS!!)
MAP           : 'faz' | 'rae' | 'any'    ;
EASING        : 'linear' | 'ease-in'     ;
DECIMAL       : '-'? [0-9]+ ('.'[0-9]+)? ;
INTEGER       : '-'? [0-9]+              ;
STRING        : '"' (~["\r\n])* '"'      ;
ID            : [a-zA-Z_][a-zA-Z0-9_]+   ;
WS            : [ \t\r\n]+ -> skip       ;
LINE_COMMENT  : '#' ~[\r\n]+ -> channel(HIDDEN) ;
EOL           : [\r\n]+                  ;
