package com.logginghub.utils.swing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.logginghub.utils.container.Changeable;
import com.logginghub.utils.container.Container;
import com.logginghub.utils.container.ContainerListener;
import com.logginghub.utils.container.ListContainer;
import com.logginghub.utils.filter.CompositeOrFilter;
import com.logginghub.utils.filter.Filter;

public class ContainerTest {
    ListContainer<String> container;
    List<AddRecord<String>> added;
    List<String> removed;
    List<MoveRecord<String>> moved;
    ContainerListener<String> listener;

    class AddRecord<T> {
        public T item;
        public int index;

        public AddRecord(T item, int index) {
            super();
            this.item = item;
            this.index = index;
        }
    }

    class MoveRecord<T> {
        public T item;
        public int newIndex;
        public int oldIndex;

        public MoveRecord(T item, int newIndex, int oldIndex) {
            super();
            this.item = item;
            this.newIndex = newIndex;
            this.oldIndex = oldIndex;
        }

        @Override public String toString() {
            return String.format("[MoveRecord] item='%s' from='%d' to='%d'", item, oldIndex, newIndex);
        }
    }

    class CompositeObject extends Changeable<CompositeObject> {
        public String text;
        public int value;

        public CompositeObject(String text, int value) {
            super();
            this.text = text;
            this.value = value;
        }

        public void incrementValue(int i) {
            value += i;
            fireChanged(this);
        }

        @Override public String toString() {
            return String.format("text = %s value = %d", text, value);
        }
    }

    @Before public void setup() {
        container = new ListContainer<String>();
        added = new ArrayList<AddRecord<String>>();
        removed = new ArrayList<String>();
        moved = new ArrayList<MoveRecord<String>>();

        listener = new ContainerListener<String>() {
            public void onItemRemoved(Container<String> container, String item, int index) {
                removed.add(item);
            }

            public void onItemAdded(Container<String> container, String item, int index) {
                added.add(new AddRecord<String>(item, index));
            }

            public void onItemMoved(Container<String> container, String item, int newIndex, int oldIndex) {
                moved.add(new MoveRecord<String>(item, newIndex, oldIndex));
            }
        };

        container.addContainerListener(listener);
    }

    @Test public void testAdd() {
        String newString = "New string";
        assertTrue(container.isEmpty());
        container.add(newString);

        assertTrue(!container.isEmpty());
        assertEquals(1, container.size());
        assertEquals(newString, container.get(0));
        assertEquals(1, added.size());
        assertEquals(0, removed.size());
        assertTrue(container.contains(newString));

    }

    @Test public void testRemove() {
        String newString = "New string";
        container.add(newString);

        assertEquals(1, container.size());
        assertEquals(1, added.size());
        assertEquals(0, removed.size());

        container.remove(newString);

        assertEquals(0, container.size());
        assertEquals(1, added.size());
        assertEquals(1, removed.size());
        assertTrue(container.isEmpty());
    }

    @Test public void testRemoveContainerListener() {
        String newString = "New string";
        container.add(newString);

        assertEquals(1, container.size());
        assertEquals(newString, container.get(0));
        assertEquals(1, added.size());
        assertEquals(0, removed.size());

        container.removeContainerListener(listener);
        container.add(newString);

        assertEquals(2, container.size());
        assertEquals(newString, container.get(0));
        assertEquals(newString, container.get(1));
        assertEquals(1, added.size());
        assertEquals(0, removed.size());
    }

    @Test public void testIterator() {
        String stringA = "StringA";
        String stringB = "StringB";
        String stringC = "StringC";

        container.add(stringA);
        container.add(stringB);
        container.add(stringC);

        Iterator<String> iterator = container.iterator();
        assertEquals(stringA, iterator.next());
        assertEquals(stringB, iterator.next());
        assertEquals(stringC, iterator.next());
    }

    @Test(expected = UnsupportedOperationException.class) public void testIteratorReadOnly() {
        String stringA = "StringA";
        String stringB = "StringB";
        String stringC = "StringC";

        container.add(stringA);
        container.add(stringB);
        container.add(stringC);

        Iterator<String> iterator = container.iterator();
        assertEquals(stringA, iterator.next());
        iterator.remove();
    }

    @Test public void testFilter() {
        String stringA = "StringA-moo";
        String stringB = "StringB-baa";
        String stringC = "StringC-moo";

        container.add(stringA);
        container.add(stringB);
        container.add(stringC);

        assertEquals(3, container.size());
        assertEquals(stringA, container.get(0));
        assertEquals(stringB, container.get(1));
        assertEquals(stringC, container.get(2));

        Filter<String> filter = new Filter<String>() {
            public boolean passes(String t) {
                return t.contains("moo");
            }
        };
        container.addFilter(filter);

        assertEquals(2, container.size());
        assertEquals(stringA, container.get(0));
        assertEquals(stringC, container.get(1));
        assertTrue(container.isVisible(stringA));
        assertFalse(container.isVisible(stringB));
        assertTrue(container.isVisible(stringC));

        container.removeFilter(filter);
        assertEquals(3, container.size());
        assertEquals(stringA, container.get(0));
        assertEquals(stringB, container.get(1));
        assertEquals(stringC, container.get(2));
        assertTrue(container.isVisible(stringA));
        assertTrue(container.isVisible(stringB));
        assertTrue(container.isVisible(stringC));

    }

    @SuppressWarnings("unchecked") @Test public void testOrFilter() {
        String stringA = "StringA-moo";
        String stringB = "StringB-baa";
        String stringC = "StringC-moo";
        String stringD = "StringB-woof";

        container.add(stringA);
        container.add(stringB);
        container.add(stringC);
        container.add(stringD);

        Filter<String> mooFilter = new Filter<String>() {
            public boolean passes(String t) {
                return t.contains("moo");
            }
        };

        Filter<String> woofFilter = new Filter<String>() {
            public boolean passes(String t) {
                return t.contains("woof");
            }
        };

        CompositeOrFilter<String> filter = new CompositeOrFilter<String>(mooFilter, woofFilter);
        container.addFilter(filter);

        assertEquals(3, container.size());
        assertEquals(stringA, container.get(0));
        assertEquals(stringC, container.get(1));
        assertEquals(stringD, container.get(2));

        container.removeFilter(filter);
        assertEquals(4, container.size());
        assertEquals(stringA, container.get(0));
        assertEquals(stringB, container.get(1));
        assertEquals(stringC, container.get(2));
        assertEquals(stringD, container.get(3));
    }

    @Ignore @Test public void testSorting() {
        String stringA = "aString";
        String stringB = "bString";
        String stringC = "cString";

        container.add(stringC);
        container.add(stringA);
        container.add(stringB);

        assertEquals(3, container.size());
        assertEquals(stringC, container.get(0));
        assertEquals(stringA, container.get(1));
        assertEquals(stringB, container.get(2));

        Comparator<String> comparator = new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        };

        container.addComparator(comparator);

        assertEquals(3, container.size());
        assertEquals(stringA, container.get(0));
        assertEquals(stringB, container.get(1));
        assertEquals(stringC, container.get(2));

        container.removeComparator(comparator);
        assertEquals(3, container.size());
        assertEquals(stringC, container.get(0));
        assertEquals(stringA, container.get(1));
        assertEquals(stringB, container.get(2));
    }

    @Ignore @Test public void testCompositeSorting() {
        CompositeObject objectA = new CompositeObject("cString", 1);
        CompositeObject objectB = new CompositeObject("aString", 0);
        CompositeObject objectC = new CompositeObject("cString", 2);

        ListContainer<CompositeObject> container = new ListContainer<CompositeObject>();

        container.add(objectA);
        container.add(objectB);
        container.add(objectC);

        assertEquals(3, container.size());
        assertEquals(objectA, container.get(0));
        assertEquals(objectB, container.get(1));
        assertEquals(objectC, container.get(2));

        Comparator<CompositeObject> comparatorOne = new Comparator<CompositeObject>() {
            public int compare(CompositeObject o1, CompositeObject o2) {
                return o1.text.compareTo(o2.text);
            }
        };

        Comparator<CompositeObject> comparatorTwo = new Comparator<CompositeObject>() {
            public int compare(CompositeObject o1, CompositeObject o2) {
                return Integer.valueOf(o2.value).compareTo(o1.value);
            }
        };

        container.addComparator(comparatorOne);
        container.addComparator(comparatorTwo);

        assertEquals(3, container.size());
        assertEquals(objectB, container.get(0));
        assertEquals(objectC, container.get(1));
        assertEquals(objectA, container.get(2));

        container.removeComparator(comparatorOne);
        assertEquals(3, container.size());
        assertEquals(objectC, container.get(0));
        assertEquals(objectA, container.get(1));
        assertEquals(objectB, container.get(2));

        container.removeComparator(comparatorTwo);
        assertEquals(3, container.size());
        assertEquals(objectA, container.get(0));
        assertEquals(objectB, container.get(1));
        assertEquals(objectC, container.get(2));
    }

    @Ignore @Test public void testOrderChanges() {
        CompositeObject firstItem = new CompositeObject("c", 3);
        CompositeObject secondItem = new CompositeObject("b", 2);
        CompositeObject thirdItem = new CompositeObject("a", 1);
        CompositeObject fouthItem = new CompositeObject("d", 4);

        ListContainer<CompositeObject> container = new ListContainer<CompositeObject>();

        final List<AddRecord<CompositeObject>> added = new ArrayList<AddRecord<CompositeObject>>();
        final List<MoveRecord<CompositeObject>> moved = new ArrayList<MoveRecord<CompositeObject>>();

        container.addContainerListener(new ContainerListener<CompositeObject>() {

            public void onItemRemoved(Container<CompositeObject> container, CompositeObject item, int index) {

            }

            public void onItemMoved(Container<CompositeObject> container, CompositeObject item, int newIndex, int oldIndex) {
                moved.add(new MoveRecord<CompositeObject>(item, newIndex, oldIndex));
            }

            public void onItemAdded(Container<CompositeObject> container, CompositeObject item, int index) {
                added.add(new AddRecord<CompositeObject>(item, index));
            }
        });

        Comparator<CompositeObject> comparator = new Comparator<CompositeObject>() {
            public int compare(CompositeObject o1, CompositeObject o2) {
                return Integer.valueOf(o1.value).compareTo(o2.value);
            }
        };

        container.addComparator(comparator);

        // Add the first item, this should not result in a move as its the first
        // one
        container.add(firstItem);
        assertEquals(firstItem, container.get(0));
        assertEquals(1, added.size());
        assertSame(firstItem, added.get(0).item);
        assertEquals(0, added.get(0).index);
        assertEquals(0, moved.size());

        // Add the second item, make sure the item appear in the correct order
        container.add(secondItem);
        assertEquals(secondItem, container.get(0));
        assertEquals(firstItem, container.get(1));

        // The second item should have been added in index 0
        assertEquals(2, added.size());
        assertSame(secondItem, added.get(1).item);
        assertEquals(0, added.get(1).index);

        // The first item should have been moved from index 0 to index 1
        assertEquals(1, moved.size());
        assertSame(firstItem, moved.get(0).item);
        assertEquals(0, moved.get(0).oldIndex);
        assertEquals(1, moved.get(0).newIndex);

        // Add the third item, make sure the item appear in the correct order
        container.add(thirdItem);
        assertEquals(thirdItem, container.get(0));
        assertEquals(secondItem, container.get(1));
        assertEquals(firstItem, container.get(2));

        // The third item should have been added in index 0
        assertEquals(3, added.size());
        assertSame(thirdItem, added.get(2).item);
        assertEquals(0, added.get(2).index);

        // There should now be three moves; the first from the previous test
        // and now two from this one
        assertEquals(3, moved.size());

        assertSame(firstItem, moved.get(2).item);
        assertEquals(1, moved.get(2).oldIndex);
        assertEquals(2, moved.get(2).newIndex);

        assertSame(secondItem, moved.get(1).item);
        assertEquals(0, moved.get(1).oldIndex);
        assertEquals(1, moved.get(1).newIndex);

        // Now the fourth item, which is in the right place already
        container.add(fouthItem);
        assertEquals(thirdItem, container.get(0));
        assertEquals(secondItem, container.get(1));
        assertEquals(firstItem, container.get(2));
        assertEquals(fouthItem, container.get(3));

        // No more moves
        assertEquals(3, moved.size());

        // And added at the right spot
        assertEquals(4, added.size());
        assertSame(fouthItem, added.get(3).item);
        assertEquals(3, added.get(3).index);
    }

    @Ignore @Test public void testChangableObject() {
        CompositeObject firstItem = new CompositeObject("a", 1);
        CompositeObject secondItem = new CompositeObject("b", 2);
        CompositeObject thirdItem = new CompositeObject("c", 3);
        CompositeObject fouthItem = new CompositeObject("d", 4);

        ListContainer<CompositeObject> container = new ListContainer<CompositeObject>();

        final List<AddRecord<CompositeObject>> added = new ArrayList<AddRecord<CompositeObject>>();
        final List<MoveRecord<CompositeObject>> moved = new ArrayList<MoveRecord<CompositeObject>>();

        container.addContainerListener(new ContainerListener<CompositeObject>() {

            public void onItemRemoved(Container<CompositeObject> container, CompositeObject item, int index) {

            }

            public void onItemMoved(Container<CompositeObject> container, CompositeObject item, int newIndex, int oldIndex) {
                moved.add(new MoveRecord<CompositeObject>(item, newIndex, oldIndex));
            }

            public void onItemAdded(Container<CompositeObject> container, CompositeObject item, int index) {
                added.add(new AddRecord<CompositeObject>(item, index));
            }
        });

        Comparator<CompositeObject> comparator = new Comparator<CompositeObject>() {
            public int compare(CompositeObject o1, CompositeObject o2) {
                return Integer.valueOf(o1.value).compareTo(o2.value);
            }
        };

        container.addComparator(comparator);

        container.add(firstItem);
        container.add(secondItem);
        container.add(thirdItem);
        container.add(fouthItem);

        // Everything should be in the right order
        assertSame(firstItem, container.get(0));
        assertSame(secondItem, container.get(1));
        assertSame(thirdItem, container.get(2));
        assertSame(fouthItem, container.get(3));

        // Now change one of the values - this should move the second item to
        // the end
        secondItem.incrementValue(5);

        assertSame(firstItem, container.get(0));
        assertSame(secondItem, container.get(3));
        assertSame(thirdItem, container.get(1));
        assertSame(fouthItem, container.get(2));

        // Check the move records
        assertEquals(3, moved.size());
        assertSame(thirdItem, moved.get(0).item);
        assertEquals(2, moved.get(0).oldIndex);
        assertEquals(1, moved.get(0).newIndex);

        assertSame(fouthItem, moved.get(1).item);
        assertEquals(3, moved.get(1).oldIndex);
        assertEquals(2, moved.get(1).newIndex);

        assertSame(secondItem, moved.get(2).item);
        assertEquals(1, moved.get(2).oldIndex);
        assertEquals(3, moved.get(2).newIndex);
    }
}
