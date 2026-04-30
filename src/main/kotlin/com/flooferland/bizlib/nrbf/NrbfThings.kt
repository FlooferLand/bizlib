@file:Suppress("unused")
package com.flooferland.bizlib.nrbf

enum class RecordType {
    SerializedStreamHeader,
    ClassWithId,
    SystemClassWithMembers,
    ClassWithMembers,
    SystemClassWithMembersAndTypes,
    ClassWithMembersAndTypes,
    BinaryObjectString,
    BinaryArray,
    MemberPrimitiveTyped,
    MemberReference,
    ObjectNull,
    MessageEnd,
    BinaryLibrary,
    ObjectNullMultiple256,
    ObjectNullMultiple,
    ArraySinglePrimitive,
    ArraySingleObject,
    ArraySingleString,
    BinaryMethodCall,
    BinaryMethodReturn;
    val id get() = ordinal
}

enum class PrimitiveType {
    Boolean,
    Byte,
    Char,
    Unused,
    Decimal,
    Double,
    Int16,
    Int32,
    Int64,
    SByte,
    Single,
    TimeSpan,
    DateTime,
    UInt16,
    UInt32,
    UInt64,
    Null,
    String;
    val id get() = ordinal + 1
}

enum class BinaryType {
    Primitive,
    String,
    Object,
    SystemClass,
    Class,
    ObjectArray,
    StringArray,
    PrimitiveArray;
    val id get() = ordinal
}


