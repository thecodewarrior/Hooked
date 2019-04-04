package games.thecodewarrior.hooked.util

import net.minecraft.nbt.*
import net.minecraft.nbt.Tag
import java.util.UUID

operator fun CompoundTag.set(key: String, value: Tag) = this.put(key, value)

operator fun CompoundTag.set(key: String, value: Boolean) = this.putBoolean(key, value)
operator fun CompoundTag.set(key: String, value: Byte) = this.putByte(key, value)
operator fun CompoundTag.set(key: String, value: Short) = this.putShort(key, value)
operator fun CompoundTag.set(key: String, value: Int) = this.putInt(key, value)
operator fun CompoundTag.set(key: String, value: Long) = this.putLong(key, value)
operator fun CompoundTag.set(key: String, value: Float) = this.putFloat(key, value)
operator fun CompoundTag.set(key: String, value: Double) = this.putDouble(key, value)

operator fun CompoundTag.set(key: String, value: UUID) = this.putUuid(key, value)
operator fun CompoundTag.set(key: String, value: String) = this.putString(key, value)

operator fun CompoundTag.set(key: String, value: ByteArray) = this.putByteArray(key, value)
operator fun CompoundTag.set(key: String, value: IntArray) = this.putIntArray(key, value)
operator fun CompoundTag.set(key: String, value: LongArray) = this.putLongArray(key, value)

@JvmName("setIntList")
operator fun CompoundTag.set(key: String, value: List<Int>) = this.putIntArray(key, value)
@JvmName("setLongList")
operator fun CompoundTag.set(key: String, value: List<Long>) = this.putLongArray(key, value)

enum class TagType {
    END, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BYTE_ARR, STRING, LIST, COMPOUND, INT_ARR, LONG_ARR;
}
val Tag.enumType: TagType get() = TagType.values()[this.type.toInt()]

fun Tag.matchesPattern(other: Tag): Boolean {
    return when {
        this is EndTag       && other is EndTag       -> true
        this is ByteTag      && other is ByteTag      -> this.byte == other.byte
        this is ShortTag     && other is ShortTag     -> this.short == other.short
        this is IntTag       && other is IntTag       -> this.int == other.int
        this is LongTag      && other is LongTag      -> this.long == other.long
        this is FloatTag     && other is FloatTag     -> this.float == other.float
        this is DoubleTag    && other is DoubleTag    -> this.double == other.double
        this is ByteArrayTag && other is ByteArrayTag -> this.byteArray.contentEquals(other.byteArray)
        this is StringTag    && other is StringTag    -> this.asString() == other.asString()
        this is ListTag      && other is ListTag      -> this.size == other.size &&
            this.zip(other).all { (it, pattern) -> it.matchesPattern(pattern) }
        this is CompoundTag  && other is CompoundTag  -> this.keys.containsAll(other.keys) &&
            other.keys.all { this.getTag(it)!!.matchesPattern(other.getTag(it)!!) }
        this is IntArrayTag  && other is IntArrayTag  -> this.intArray.contentEquals(other.intArray)
        this is LongArrayTag && other is LongArrayTag -> this.longArray.contentEquals(other.longArray)
        else -> false
    }
}
