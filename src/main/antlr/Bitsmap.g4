grammar Bitsmap;
file: setStmt+ bitStmt* EOF;

// Set statement
setStmt: 'set' MAP fixture;

// Bit statement
bitStmt: mappedMovement (',' mappedMovement)* '{' bitFields+ '}';
bitFields: flowField | animField | rotateField | moveField;

// Bit fields
flowField: 'flow' DECIMAL;
animField: 'anim' STRING;
rotateField: 'rotate' bone vec3i;
moveField: 'move' bone vec3i;

// Thingies
mappedMovement: MAP movement;
vec3i: iaxisX? iaxisY? iaxisZ?;
iaxisX: 'x' ':' INTEGER;
iaxisY: 'y' ':' INTEGER;
iaxisZ: 'z' ':' INTEGER;
bone: STRING;
movement: ID;
fixture: ID;

// Lexer (NOTE: ORDER MATTERS!!)
MAP           : 'faz' | 'rae' | 'any'    ;
INTEGER       : '-'? [0-9]+              ;
DECIMAL       : '-'? [0-9]+ ('.'[0-9]+)? ;
STRING        : '"' (~["\r\n])* '"'      ;
ID            : [a-zA-Z_][a-zA-Z0-9_]+   ;
WS            : [ \t\r\n]+ -> skip       ;
LINE_COMMENT  : '#' ~[\r\n]+ -> channel(HIDDEN) ;
EOL           : [\r\n]+                  ;
