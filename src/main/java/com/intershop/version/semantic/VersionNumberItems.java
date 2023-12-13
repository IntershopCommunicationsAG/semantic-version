/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.intershop.version.semantic;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

class VersionNumberItems
{
    private static final int MAX_INTITEM_LENGTH = 9;
    private static final int MAX_LONGITEM_LENGTH = 18;
    // cache for the numbers 0-99
    private static final Map<String, IntItem> ITEM_CACHE = new ConcurrentHashMap<>();

    public static Item parseItem(String buf)
    {
        if (buf == null || buf.isEmpty())
        {
            throw new IllegalArgumentException("invalid empty version string");
        }
        buf = stripLeadingZeroes(buf);
        if (buf.length() <= MAX_INTITEM_LENGTH)
        {
            // lower than 2^31
            return IntItem.valueOf(buf);
        }
        else if (buf.length() <= MAX_LONGITEM_LENGTH)
        {
            // lower than 2^63
            return LongItem.valueOf(buf);
        }
        return BigIntegerItem.valueOf(buf);
    }

    private static String stripLeadingZeroes(String buf)
    {
        for (int i = 0; i < buf.length(); ++i)
        {
            char c = buf.charAt(i);
            if (c != '0')
            {
                return buf.substring(i);
            }
        }
        return buf;
    }

    enum ItemType
    {
        INT, LONG, BIGINTEGER
    };

    public interface Item
    {
        /**
         * @param item (required)
         * @return the value 0 if x == y; a value less than 0 if x < y; and a value greater than 0 if x > y
         */
        int compareTo(Item item);

        /**
         * @return type (incl. size) of item
         */
        ItemType getType();

        /**
         * @return true of numbers are zero only
         */
        boolean isNull();

        /**
         * @return item which represents item++
         */
        Item increment();
    }

    /**
     * Represents a numeric item in the version item list that can be represented with an int.
     */
    private static class IntItem implements Item
    {
        static IntItem valueOf(String str)
        {
            if (str.length() < 3)
            {
                return ITEM_CACHE.computeIfAbsent(str, (s) -> new IntItem(Integer.parseInt(s)));
            }
            return new IntItem(Integer.parseInt(str));
        }

        private final int value;
        IntItem(int value)
        {
            this.value = value;
        }

        @Override
        public ItemType getType()
        {
            return ItemType.INT;
        }

        @Override
        public boolean isNull()
        {
            return value == 0;
        }

        @Override
        public int compareTo(Item item)
        {
            if (this == item)
            {
                return 0;
            }
            switch(item.getType())
            {
                case INT:
                    int itemValue = ((IntItem)item).value;
                    return Integer.compare(value, itemValue);
                case LONG:
                case BIGINTEGER:
                    return -1;
                default:
                    throw new IllegalStateException("invalid item: " + item.getClass());
            }
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            IntItem intItem = (IntItem)o;

            return value == intItem.value;
        }

        @Override
        public int hashCode()
        {
            return value;
        }

        @Override
        public String toString()
        {
            return Integer.toString(value);
        }

        @Override
        public Item increment()
        {
            int inc = value + 1;
            return inc < 100 ? IntItem.valueOf((Integer.valueOf(inc).toString())) : new IntItem(inc);
        }
    }

    /**
     * Represents a numeric item in the version item list that can be represented with a long.
     */
    private static class LongItem implements Item
    {
        static LongItem valueOf(String str)
        {
            return new LongItem(Long.parseLong(str));
        }

        private final long value;
        LongItem(long value)
        {
            this.value = value;
        }

        @Override
        public ItemType getType()
        {
            return ItemType.LONG;
        }

        @Override
        public boolean isNull()
        {
            return value == 0;
        }

        @Override
        public int compareTo(Item item)
        {
            if (item == null)
            {
                return (value == 0) ? 0 : 1; // 1.0 == 1, 1.1 > 1
            }

            switch(item.getType())
            {
                case INT:
                    return 1;
                case LONG:
                    long itemValue = ((LongItem)item).value;
                    return Long.compare(value, itemValue);
                case BIGINTEGER:
                    return -1;

                default:
                    throw new IllegalStateException("invalid item: " + item.getClass());
            }
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            LongItem longItem = (LongItem)o;

            return value == longItem.value;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(value);
        }

        @Override
        public String toString()
        {
            return Long.toString(value);
        }

        @Override
        public Item increment()
        {
            long inc = value + 1;
            return inc > 999_999_999 ? parseItem((Long.valueOf(inc).toString())) : new LongItem(inc);
        }
    }

    /**
     * Represents a numeric item in the version item list.
     */
    private static class BigIntegerItem implements Item
    {
        static BigIntegerItem valueOf(String str)
        {
            return new BigIntegerItem(new BigInteger(str));
        }

        private final BigInteger value;
        BigIntegerItem(BigInteger value)
        {
            this.value = value;
        }

        @Override
        public ItemType getType()
        {
            return ItemType.BIGINTEGER;
        }

        @Override
        public boolean isNull()
        {
            return BigInteger.ZERO.equals(value);
        }

        @Override
        public int compareTo(Item item)
        {
            switch(item.getType())
            {
                case INT:
                case LONG:
                    return 1;

                case BIGINTEGER:
                    return value.compareTo(((BigIntegerItem)item).value);

                default:
                    throw new IllegalStateException("invalid item: " + item.getClass());
            }
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            BigIntegerItem that = (BigIntegerItem)o;

            return value.equals(that.value);
        }

        @Override
        public int hashCode()
        {
            return value.hashCode();
        }

        public String toString()
        {
            return value.toString();
        }

        @Override
        public Item increment()
        {
            return new BigIntegerItem(value.add(BigInteger.ONE));
        }
    }
}
