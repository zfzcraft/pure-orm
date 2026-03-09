package cn.zfz.pureorm.crud.condition;

public enum ConditionType {
    EQ, 
    NE, 
    GT, 
    GE, 
    LT, 
    LE,
    LIKE, 
    LIKE_LEFT, 
    LIKE_RIGHT,
    IN, 
    NOT_IN,
    IS_NULL, 
    IS_NOT_NULL,
    BETWEEN,
    AND, 
    OR,
    LEFT_PAREN, 
    RIGHT_PAREN,
    NATIVE
}