package com.redhat.osas.amqp.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;

public class AMQPTypesCodec {
	public static Object decode(Type type, ChannelBuffer buffer) {
		switch(type) {
		case STR8:
			return decodeStr8(buffer);
		case STR16:
			return decodeStr16(buffer);
		case MAP:
			return decodeMap(buffer);
		case ARRAY:
			return decodeArray(buffer);
		case UINT16:
			return decodeUint16(buffer);
		case VBIN16:
		    return decodeVBin16(buffer);
		case SEQUENCE_NO:
		    return decodeSequenceNumber(buffer);
		case SEQUENCE_SET:
		    return decodeSequenceSet(buffer);
		case UINT64:
		    return decodeUInt64(buffer);
		case UINT8:
		    return decodeUint8(buffer);
		case DATETIME:
		    return decodeDateTime(buffer);
		}
		
		throw new IllegalArgumentException("Received type " + type + ", but don't have a decoder for it");
	}

    @SuppressWarnings("unchecked")
    public static void encode(Type type, Object o, ChannelBuffer buffer) {
		switch(type) {
		case STR8:
			encodeStr8((String)o, buffer);
			break;
		case STR16:
			encodeStr16((String)o, buffer);
			break;
		case MAP:
			encodeMap((Map<KeyType, Object>)o, buffer);
			break;
		case ARRAY:
			encodeArray(type, (List<Object>)o, buffer);
			break;
		case VBIN16:
		    encodeVBin16(o, buffer);
		    break;
		case VBIN32:
			encodeVBin32(o, buffer);
			break;
		case UINT8:
		    encodeUint8((Short)o, buffer);
		    break;
		case UINT16:
			encodeUint16((Integer)o, buffer);
			break;
		case SEQUENCE_NO:
		    encodeUint32(((Long)o).intValue(), buffer);
		    break;
		case UINT32:
		    encodeUint32(o, buffer);
		    break;
		case UINT64:
		    encodeUint64((Long)o, buffer);
		    break;
		case SEQUENCE_SET:
		    ((SequenceSet)o).encode(buffer);
		    break;
		default:
		    throw new IllegalArgumentException("Unable to encode type " + type);
			    
		}
		
	}
	
	@SuppressWarnings("unused")
    public static Map<KeyType, Object> decodeMap(ChannelBuffer buffer) {
		Map<KeyType, Object> m = new HashMap<KeyType, Object>();
		
		long size = buffer.readUnsignedInt();
		long numEntries = buffer.readUnsignedInt();
		
		for (long i = 0; i < numEntries; i++) {
			String key = decodeStr8(buffer);
			short dataType = buffer.readUnsignedByte();
			Object value = decode(Type.getByCode(dataType), buffer);
			m.put(new KeyType(key, Type.getByCode(dataType)), value);
		}
		
		return m;
	}
	
	public static void encodeMap(Map<KeyType, Object> map, ChannelBuffer buffer) {
		int size = 4;
		Set<KeyType> keys = map.keySet();
		for (KeyType key : keys) {
			size += encodedSize(Type.STR8, key.key);
			size++;
			size += encodedSize(key.type, map.get(key));
		}
		
		buffer.writeInt(size);
		buffer.writeInt(keys.size());
		for (KeyType key : keys) {
			encodeStr8(key.key, buffer);
			buffer.writeByte(key.type.getCode());
			encode(key.type, map.get(key), buffer);
		}
	}
	
	
	public static String decodeStr8(ChannelBuffer buffer) {
		short length = buffer.readUnsignedByte();
		byte[] content = new byte[length];
		buffer.readBytes(content);
		return new String(content);
	}
	
	public static void encodeStr8(String s, ChannelBuffer buffer) {
		buffer.writeByte(s.length());
		buffer.writeBytes(s.getBytes());
	}
	
	public static int encodedSize(Type type, Object o) {
		switch(type) {
		case BIT:
		    return 0;
		case UINT8:
		case INT8:
		    return 1;
		case UINT16:
		case INT16:
		    return 2;
		case UINT32:
		case INT32:
		    return 4;
		case UINT64:
		case INT64:
		    return 8;
		case STR8:
			return 1 + ((String)o).length();
		case STR16:
			return 2 + ((String)o).length();
		case DATETIME:
		    return 8;
		}
		
		throw new RuntimeException("Don't know how to calculate encoded size of " + type.getName());
	}
	
	public static String decodeStr16(ChannelBuffer buffer) {
		int length = buffer.readUnsignedShort();
		byte[] content = new byte[length];
		buffer.readBytes(content);
		return new String(content);
	}
	
	public static void encodeStr16(String s, ChannelBuffer buffer) {
		buffer.writeShort(s.length());
		buffer.writeBytes(s.getBytes());
	}
	
	public static List<Object> decodeArray(ChannelBuffer buffer) {
		List<Object> list = new ArrayList<Object>();
		
		@SuppressWarnings("unused")
        long length = buffer.readUnsignedInt();
		
		short objectType = buffer.readUnsignedByte();
		
		long objectCount = buffer.readUnsignedInt();
		
		for(long i = 0; i < objectCount; i++) {
			list.add(decode(Type.getByCode(objectType), buffer));
		}
		return list;
	}
	
	public static void encodeArray(Type type, List<Object> array, ChannelBuffer buffer) {
		int arrayByteSize = 5;
		for (Object object : array) {
			arrayByteSize += encodedSize(type, object);
		}
		buffer.writeInt(arrayByteSize);
		buffer.writeByte(type.getCode());
		buffer.writeInt(array.size());
		for (Object object : array) {
			encode(type, object, buffer);
		}
	}
	
	public static void encodeVBin16(Object o, ChannelBuffer buffer) {
	    encodeVBin(2, o, buffer);
	}
	
	public static void encodeVBin32(Object o, ChannelBuffer buffer) {
	    encodeVBin(4, o, buffer);
	}
	
	private static void encodeVBin(int sizeWidth, Object o, ChannelBuffer buffer) {
	    int size = 0;
	    if(o instanceof String) {
	        size = ((String)o).length();
	    }
	    
	    switch(sizeWidth) {
	    case 1:
	        buffer.writeByte(size);
	        break;
	    case 2:
	        buffer.writeShort(size);
	        break;
	    case 4:
	        //TODO does size need to be a long in this case?
	        buffer.writeInt(size);
	        break;
	    }
	    
	    if(size > 0) {
    	    if (o instanceof String) {
    	        buffer.writeBytes(((String)o).getBytes());
    	    }
	    }
	}
	
	public static byte[] decodeVBin16(ChannelBuffer buffer) {
	    int size = buffer.readUnsignedShort();
	    byte[] data = new byte[size];
	    buffer.readBytes(data);
	    return data;
	}
	
	public static void encodeUint8(short i, ChannelBuffer buffer) {
	    buffer.writeByte(i);
	}
	
	public static void encodeUint16(int i, ChannelBuffer buffer) {
		buffer.writeShort(i);
	}
	
	public static void encodeUint32(Object o, ChannelBuffer buffer) {
	    int i = 0;
	    if (o instanceof Integer) {
	        i = (Integer)o;
	    } else if (o instanceof Long) {
	        i = ((Long)o).intValue();
	    }
	    buffer.writeInt(i);
	}
	
	public static void encodeUint64(long i, ChannelBuffer buffer) {
	    buffer.writeLong(i);
	}
	
	public static int decodeUint16(ChannelBuffer buffer) {
		return buffer.readUnsignedShort();
	}
	
	public static short decodeUint8(ChannelBuffer buffer) {
	    return buffer.readUnsignedByte();
	}
	
	public static long decodeSequenceNumber(ChannelBuffer buffer) {
	    return buffer.readUnsignedInt();
	}
	
	public static long decodeUInt64(ChannelBuffer buffer) {
	    long high = buffer.readUnsignedInt();
	    long low = buffer.readUnsignedInt();
	    long ret = (high << 32) & low;
	    return ret;
	}
	
	public static SequenceSet decodeSequenceSet(ChannelBuffer buffer) {
	    SequenceSet s = new SequenceSet();
	    s.decode(buffer);
	    return s;
	}
	
	public static long decodeDateTime(ChannelBuffer buffer) {
	    long time = buffer.readLong();
//	    Calendar cal = Calendar.getInstance();
//	    cal.setTimeInMillis(time);
//	    return cal;
	    return time;
	}
}
