grammar Bitsmap;
file: prepStmt* setStmt+ bitStmt* EOF;

// Set statement
setStmt: 'set' MAP fixture;

// Bit statement
bitStmt: mappedMovement (',' mappedMovement)* '{' bitFields+ '}';
bitFields:
	flowField | animField | rotateField | moveField | typeField | holdField | wiggleMulField
;

// Bit fields
flowField: 'flow' EASING? num;
animField: 'anim' STRING;
rotateField: 'rotate' bone vec3i;
moveField: 'move' bone vec3i;
typeField: 'type' MOVE_TYPE;
holdField: 'hold' BOOLEAN;
wiggleMulField: 'wigglemul' num;

// Prep statement
prepStmt: '!' prepFields;
prepFields:
	versionPrep | '_'
;

// Prep fields
versionPrep: 'version' INTEGER;

// Thingies
mappedMovement: MAP bit;
vec3i: iaxisX? iaxisY? iaxisZ?;
iaxisX: 'x' ':' num;
iaxisY: 'y' ':' num;
iaxisZ: 'z' ':' num;
bone: STRING;
bit: ID | INTEGER | DRAWER_BIT;
fixture: ID;  // for setStmt
num: INTEGER | DECIMAL;

// Lexer (NOTE: ORDER MATTERS!!)
fragment DIGIT : [0-9] ;
fragment UINT  : DIGIT+ ;
DRAWER_BIT     : UINT('td'|'bd')          ;
MAP            : 'faz' | 'rae' | 'any'    ;
EASING         : 'linear' | 'ease-in'     ;
MOVE_TYPE      : 'servo' | 'pneumatic'    ;
BOOLEAN        : 'yes' | 'no'             ;

DECIMAL        : '-'? [0-9]+ ('.'[0-9]+) ;
INTEGER        : '-'? UINT                ;

STRING         : '"' (~["\r\n])* '"'      ;
ID             : [a-zA-Z_][a-zA-Z0-9_]+   ;

WS             : [ \t\r\n]+ -> skip       ;
LINE_COMMENT   : '#' ~[\r\n]+ -> channel(HIDDEN) ;
EOL            : [\r\n]+                  ;
