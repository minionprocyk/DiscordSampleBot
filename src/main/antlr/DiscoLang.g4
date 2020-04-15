grammar DiscoLang;

root: MUSIC TREE command;
command:
      T_PLAY WORD+                     #player
    | VOLUME (T_INCREASE | T_DECREASE) #volume
    | (T_INCREASE | T_DECREASE) VOLUME #volume
    | STOP 's'?                        #stop
    | DO SOMETHING 's'?                #random
    ;

//lexer rules
T_INCREASE: INCREASE | UP   ;
T_DECREASE: DECREASE | DOWN ;
T_PLAY: (PLAY | PLAYED| PLAYER | PLAYING) ;
MUSIC: 'music'       ;
TREE: 'tree' 's'?    ;
PLAY: 'play'         ;
PLAYED: 'played'     ;
PLAYER: 'player'     ;
PLAYING: 'playing'   ;
DO: 'do'             ;
SOMETHING: 'something' ;
VOLUME: 'volume'     ;
INCREASE: 'increase' ;
DECREASE: 'decrease' ;
STOP: 'stop'         ;
UP: 'up'             ;
DOWN: 'down'         ;
WORD: Letter+        ;

fragment Letter
    : [a-zA-Z$_] // these are the "java letters" below 0x7F
    | ~[\u0000-\u007F\uD800-\uDBFF] // covers all characters above 0x7F which are not value surrogate
    | [\uD800-\uDBFF] [\uDC00-\uDFFF] // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
    ;

WS  :  [ \t\r\n\u000C]+ -> skip ;
