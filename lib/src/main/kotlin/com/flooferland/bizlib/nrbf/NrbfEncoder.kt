package com.flooferland.bizlib.nrbf

import java.io.DataOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.collections.forEachIndexed

class NrbfEncoder(out: OutputStream, val order: ByteOrder = ByteOrder.LITTLE_ENDIAN, block: (NrbfEncoder.() -> Unit)? = null) : AutoCloseable {
    val output: DataOutputStream = DataOutputStream(out)
    val intBuffer: ByteBuffer = ByteBuffer.allocate(4).order(order)

    var libraryId = 0

    val assemblyId = 2;

    init {
        block?.invoke(this)
        messageEnd()
    }

    fun byte(v: Int) {
        output.writeByte(v)
    }

    fun bytes(bytes: ByteArray) {
        output.write(bytes)
    }

    fun int32(v: Int) {
        bytes(getInt32(v))
    }

    fun string(v: String) {
        val bytes = v.toByteArray(Charsets.UTF_8)
        byte(bytes.size)
        bytes(bytes)
    }

    fun libraryId(id: Int = libraryId++): Int {
        int32(id)
        return id
    }

    fun objectId(id: Int = libraryId++): Int {
        int32(id)
        return id
    }

    private fun arrayInfo(id: Int, size: Int) {
        objectId(id)
        int32(size)
    }

    private fun arraySinglePrimitive(id: Int, size: Int, type: PrimitiveType, bytes: ByteArray) {
        byte(RecordType.ArraySinglePrimitive.id)  // Type
        arrayInfo(id, size)
        byte(type.id)
        bytes(bytes)
    }
    fun arraySinglePrimitive(id: Int, v: ByteArray) {
        arraySinglePrimitive(id, v.size, PrimitiveType.Byte, v)
    }
    fun arraySinglePrimitive(id: Int, v: IntArray) {
        val data = ByteBuffer.allocate(v.size * Int.SIZE_BYTES).order(order)
        v.forEach { data.putInt(it) }
        arraySinglePrimitive(id, v.size, PrimitiveType.Int32, data.array())
    }

    fun serializationHeader() {
        byte(RecordType.SerializedStreamHeader.id)  // Type
        int32(1)  // RootId
        int32(-1) // HeaderId
        int32(1)  // Major version
        int32(0)  // Minor version
    }

    fun binaryLibrary(assemblyName: String) {
        byte(RecordType.BinaryLibrary.id)
        libraryId(assemblyId)
        string("$assemblyName, Version=0.0.0.0, Culture=neutral, PublicKeyToken=null")
    }

    private fun classInfo(name: String, members: Array<ArrayMember>) {
        objectId(1)
        string(name)
        int32(members.size)
        members.forEach { string("<${it.name}>k__BackingField") }
    }

    private fun memberTypeInfo(members: Array<ArrayMember>) {
        members.forEach { _ -> byte(BinaryType.PrimitiveArray.id) }
        members.forEach { byte(it.type.id) }
    }

    fun clazz(name: String, members: Array<ArrayMember>) {
        val hasVideo = false  // TODO: Add video support later
        byte(RecordType.ClassWithMembersAndTypes.id)
        classInfo(name, members)
        memberTypeInfo(members)
        libraryId(assemblyId)
        byte(0x09); int32(3)  // Audio
        byte(0x09); int32(4)  // Signal
        if (hasVideo) { byte(0x09); int32(5) } else byte(0x0A)
    }

    fun messageEnd() {
        byte(0x0B)
    }

    fun getInt32(v: Int): ByteArray {
        intBuffer.clear()
        return intBuffer.putInt(v).array()
    }

    override fun close() {

    }

    data class ArrayMember(val name: String, val type: PrimitiveType)
}