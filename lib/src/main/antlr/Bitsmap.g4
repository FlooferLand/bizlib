grammar Bitsmap;
file: prepStmt* setStmt+ bitStmt* EOF;

// Set statement
setStmt: 'set' map fixture?;

// Bit statement
bitStmt: mappedMovement (',' mappedMovement)* '{' bitField+ '}';
bitField:
	flowField | animField | rotateField | moveField | typeField | holdField | wiggleMulField
;

// Bit fields
flowField: 'flow' (oldFlowFieldParams | params);
animField: 'anim' STRING;
rotateField: 'rotate' bone params;
moveField: 'move' bone params;
typeField: 'type' MOVE_TYPE;
holdField: 'hold' BOOLEAN;
wiggleMulField: 'wigglemul' num;

oldFlowFieldParams: EASING? num;

params: unnamedParam* namedParam*;
unnamedParam: paramValue;
namedParam: paramName ':' paramValue;
paramName: ID;
paramValue: num | BOOLEAN | MOVE_TYPE | FLOW_TYPE | EASING | STRING;

// Prep statement
prepStmt: '!' prepFields;
prepFields:
	versionPrep | '_'
;

// Prep fields
versionPrep: 'version' INTEGER;

// Thingies
mappedMovement: map bit;
bone: STRING;
bit: ID | INTEGER | DRAWER_BIT;
fixture: ID;  // for setStmt
map: ID;
num: INTEGER | DECIMAL;

// Lexer (NOTE: ORDER MATTERS!!)
fragment DIGIT : [0-9] ;
fragment UINT  : DIGIT+ ;
DRAWER_BIT     : UINT('td'|'bd')          ;
EASING         : 'linear' | 'ease-in'     ;
MOVE_TYPE      : 'servo' | 'pneumatic' | 'effect' ;
FLOW_TYPE      : 'speed' | 'accel' | 'smash' | 'smash_speed' ;
BOOLEAN        : 'yes' | 'no'             ;

DECIMAL        : '-'? [0-9]+ ('.'[0-9]+)  ;
INTEGER        : '-'? UINT                ;

STRING         : '"' (~["\r\n])* '"'      ;
ID             : [a-zA-Z_][a-zA-Z0-9_]*   ;

WS             : [ \t\r\n]+ -> skip       ;
LINE_COMMENT   : '#' ~[\r\n]+ -> channel(HIDDEN) ;
CARET          : '\u0001' ;
