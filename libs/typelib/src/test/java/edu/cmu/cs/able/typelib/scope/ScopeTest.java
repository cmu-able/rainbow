package edu.cmu.cs.able.typelib.scope;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Tests scopes and scoped objects.
 */
@SuppressWarnings("javadoc")
public class ScopeTest extends DefaultTCase {
	@Test
	public void add_find_scoped_objects_in_scope() throws Exception {
		TestScope s = new TestScope();
		ScopedObject so1 = new ScopedObject("o1");
		s.add(so1);
		ScopedObject so2 = new ScopedObject("o2");
		s.add(so2);
		
		assertEquals(so1, s.find("o1"));
		assertEquals(so2, s.find("o2"));
		assertNull(s.find("o3"));
		
		assertEquals(so1, s.find(new HierarchicalName(true, "o1")));
		assertEquals(so2, s.find(new HierarchicalName(false, "o2")));
		assertNull(s.find(new HierarchicalName(true, "o3")));
		assertNull(s.find(new HierarchicalName(false, "o3")));
		assertNull(s.find(new HierarchicalName(true, "", "o1")));
		assertNull(s.find(new HierarchicalName(false, "", "o1")));
	}
	
	@Test
	public void find_objects_sub_scopes() throws Exception {
		TestScope s1 = new TestScope();
		TestScope s2 = new TestScope("s2");
		s1.add(s2);
		TestScope s3 = new TestScope("s3");
		s1.add(s3);
		
		ScopedObject so2 = new ScopedObject("o2");
		s2.add(so2);
		ScopedObject so3 = new ScopedObject("o3");
		s3.add(so3);
		
		assertNull(s1.find("o1"));
		assertNull(s1.find("o2"));
		assertNull(s1.find("o3"));
		assertNull(s2.find("o1"));
		assertEquals(so2, s2.find("o2"));
		assertNull(s2.find("o3"));
		assertNull(s3.find("o1"));
		assertNull(s3.find("o2"));
		assertEquals(so3, s3.find("o3"));
		
		assertNull(s1.find(new HierarchicalName(false, "o1")));
		assertNull(s1.find(new HierarchicalName(false, "o2")));
		assertNull(s1.find(new HierarchicalName(false, "o3")));
		assertNull(s2.find(new HierarchicalName(false, "o1")));
		assertEquals(so2, s2.find(new HierarchicalName(false, "o2")));
		assertNull(s2.find(new HierarchicalName(false, "o3")));
		assertNull(s3.find(new HierarchicalName(false, "o1")));
		assertNull(s3.find(new HierarchicalName(false, "o2")));
		assertEquals(so3, s3.find(new HierarchicalName(false, "o3")));
		
		assertNull(s1.find(new HierarchicalName(false, "o1")));
		assertNull(s1.find(new HierarchicalName(false, "o2")));
		assertNull(s1.find(new HierarchicalName(false, "o3")));
		assertNull(s2.find(new HierarchicalName(false, "o1")));
		assertEquals(so2, s2.find(new HierarchicalName(false, "o2")));
		assertNull(s2.find(new HierarchicalName(false, "o3")));
		assertNull(s3.find(new HierarchicalName(false, "o1")));
		assertNull(s3.find(new HierarchicalName(false, "o2")));
		assertEquals(so3, s3.find(new HierarchicalName(false, "o3")));
		
		assertNull(s1.find(new HierarchicalName(false, "s1", "o1")));
		assertNull(s1.find(new HierarchicalName(false, "s1", "o2")));
		assertNull(s1.find(new HierarchicalName(false, "s1", "o3")));
		assertNull(s2.find(new HierarchicalName(false, "s1", "o1")));
		assertNull(s2.find(new HierarchicalName(false, "s1", "o2")));
		assertNull(s2.find(new HierarchicalName(false, "s1", "o3")));
		assertNull(s3.find(new HierarchicalName(false, "s1", "o1")));
		assertNull(s3.find(new HierarchicalName(false, "s1", "o2")));
		assertNull(s3.find(new HierarchicalName(false, "s1", "o3")));
		
		assertNull(s1.find(new HierarchicalName(false, "s2", "o1")));
		assertEquals(so2, s1.find(new HierarchicalName(false, "s2", "o2")));
		assertNull(s1.find(new HierarchicalName(false, "s2", "o3")));
		assertNull(s2.find(new HierarchicalName(false, "s2", "o1")));
		assertNull(s2.find(new HierarchicalName(false, "s2", "o2")));
		assertNull(s2.find(new HierarchicalName(false, "s2", "o3")));
		assertNull(s3.find(new HierarchicalName(false, "s2", "o1")));
		assertNull(s3.find(new HierarchicalName(false, "s2", "o2")));
		assertNull(s3.find(new HierarchicalName(false, "s2", "o3")));
		
		assertNull(s1.find(new HierarchicalName(false, "s3", "o1")));
		assertNull(s1.find(new HierarchicalName(false, "s3", "o2")));
		assertEquals(so3, s1.find(new HierarchicalName(false, "s3", "o3")));
		assertNull(s2.find(new HierarchicalName(false, "s3", "o1")));
		assertNull(s2.find(new HierarchicalName(false, "s3", "o2")));
		assertNull(s2.find(new HierarchicalName(false, "s3", "o3")));
		assertNull(s3.find(new HierarchicalName(false, "s3", "o1")));
		assertNull(s3.find(new HierarchicalName(false, "s3", "o2")));
		assertNull(s3.find(new HierarchicalName(false, "s3", "o3")));
	}
	
	@Test
	public void find_objects_linked_scopes() throws Exception {
		TestScope s1 = new TestScope();
		TestScope s2 = new TestScope("s2");
		s1.add(s2);
		TestScope s4 = new TestScope();
		TestScope s3 = new TestScope("s3");
		s4.add(s3);
		s1.link(s3);
		s2.link(s3);
		
		ScopedObject so1 = new ScopedObject("o1");
		s1.add(so1);
		ScopedObject so2 = new ScopedObject("o2");
		s2.add(so2);
		ScopedObject so3 = new ScopedObject("o3");
		s3.add(so3);
		ScopedObject so1_3 = new ScopedObject("o1");
		s3.add(so1_3);
		ScopedObject so2_3 = new ScopedObject("o2");
		s3.add(so2_3);
		
		assertEquals(so1, s1.find("o1"));
		assertEquals(so2_3, s1.find("o2"));
		assertEquals(so3, s1.find("o3"));
		
		assertEquals(so2, s2.find("o2"));
		assertEquals(so1_3, s2.find("o1"));
		assertEquals(so3, s2.find("o3"));
		
		assertEquals(so1_3, s3.find("o1"));
		assertEquals(so2_3, s3.find("o2"));
		assertEquals(so3, s3.find("o3"));
		
		assertEquals(so1_3, s1.find(new HierarchicalName(false, "s2", "o1")));
		assertNull(s1.find(new HierarchicalName(false, "s3", "o1")));
		assertEquals(so2, s1.find(new HierarchicalName(false, "s2", "o2")));
	}
	
	@Test
	public void global_scopes() throws Exception {
		TestScope gs = new TestScope();
		assertNull(gs.parent());
		assertNull(gs.name());
	}
	
	@Test(expected = AssertionError.class)
	public void global_scopes_cannot_be_added_to_other_scopes()
			throws Exception {
		TestScope gs = new TestScope();
		TestScope gs2 = new TestScope();
		gs.add(gs2);
	}
	
	@Test
	public void adding_duplicate_objects_in_scope() throws Exception {
		TestScope s = new TestScope();
		s.add(new ScopedObject("s1"));
		s.add(new Scope<>("s2"));
		
		try {
			s.add(new ScopedObject("s1"));
			fail();
		} catch (IllegalStateException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			s.add(new TestScope("s1"));
			fail();
		} catch (IllegalStateException e) {
			/*
			 * Expected.
			 */
		}
		
		
		try {
			s.add(new ScopedObject("s2"));
			fail();
		} catch (IllegalStateException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			s.add(new TestScope("s2"));
			fail();
		} catch (IllegalStateException e) {
			/*
			 * Expected.
			 */
		}
	}
	
	@Test
	public void creating_cyclic_linkage_structures() throws Exception {
		TestScope s1 = new TestScope();
		TestScope s2 = new TestScope();
		TestScope s3 = new TestScope();
		
		s1.link(s2);
		s3.link(s1);
		
		try {
			s2.link(s1);
			fail();
		} catch (CyclicScopeLinkageException e) {
			/*
			 * Expected.
			 */
		}
		
		try {
			s2.link(s3);
			fail();
		} catch (CyclicScopeLinkageException e) {
			/*
			 * Expected.
			 */
		}
	}
	
	@SuppressWarnings("unused")
	@Test(expected = AssertionError.class)
	public void creating_empty_name() throws Exception {
		new Scope<>("");
	}
	
	@Test(expected = AssertionError.class)
	public void adding_null_scope_to_scope() throws Exception {
		TestScope s = new TestScope();
		s.add((TestScope) null);
	}
	
	@Test(expected = AssertionError.class)
	public void adding_null_scoped_object_to_scope() throws Exception {
		TestScope s = new TestScope();
		s.add((ScopedObject) null);
	}
	
	@SuppressWarnings("unused")
	@Test(expected = AssertionError.class)
	public void create_scoped_object_with_null_name() throws Exception {
		new ScopedObject(null);
	}
	
	@Test(expected = AssertionError.class)
	public void linking_null_scope() throws Exception {
		TestScope s = new TestScope();
		s.link(null);
	}
	
	@Test(expected = AssertionError.class)
	public void finding_object_with_null_string() throws Exception {
		TestScope s = new TestScope();
		s.find((String) null);
	}
	
	@Test(expected = AssertionError.class)
	public void finding_object_with_null_hierarchical_name() throws Exception {
		TestScope s = new TestScope();
		s.find((HierarchicalName) null);
	}
	
	@Test(expected = AssertionError.class)
	public void finding_scope_with_null_string() throws Exception {
		TestScope s = new TestScope();
		s.find_scope((String) null);
	}
	
	@Test(expected = AssertionError.class)
	public void finding_scope_with_null_hierarchical_name() throws Exception {
		TestScope s = new TestScope();
		s.find_scope((HierarchicalName) null);
	}
	
	@Test
	public void linking_to_subscopes() throws Exception {
		TestScope s = new TestScope();
		TestScope ss = new TestScope("s");
		s.add(ss);
		ScopedObject o = new ScopedObject("o");
		ss.add(o);
		s.link(ss);
		
		assertEquals(o, s.find("o"));
		assertEquals(o, s.find(new HierarchicalName(false, "o")));
		assertEquals(o, s.find(new HierarchicalName(false, "s", "o")));
		
		TestScope ss2 = new TestScope("s2");
		s.add(ss2);
		ScopedObject o2 = new ScopedObject("o2");
		s.add(o2);
		ss2.link(s);
		
		assertEquals(o, ss2.find("o"));
		assertEquals(o, ss2.find(new HierarchicalName(false, "o")));
		assertEquals(o, ss2.find(new HierarchicalName(false, "s", "o")));
		assertEquals(o2, ss2.find(new HierarchicalName(false,
				"s2", "s2", "o2")));
	}
	
	@Test
	public void searching_with_absolute() throws Exception {
		TestScope s1 = new TestScope();
		ScopedObject so1 = new ScopedObject("o1");
		s1.add(so1);
		TestScope s2 = new TestScope("s2");
		s1.add(s2);
		ScopedObject so2 = new ScopedObject("o2");
		s2.add(so2);
		
		assertNull(s2.find("o1"));
		assertNull(s2.find(new HierarchicalName(false, "o1")));
		assertEquals(so1, s2.find(new HierarchicalName(true, "o1")));
		
		assertEquals(so2, s2.find("o2"));
		assertNull(s2.find(new HierarchicalName(false, "s2", "o2")));
		assertEquals(so2, s2.find(new HierarchicalName(true, "s2", "o2")));
	}
	
	@Test
	public void searching_ambiguous_objects() throws Exception {
		TestScope s = new TestScope();
		TestScope s1 = new TestScope();
		TestScope s2 = new TestScope();
		s.link(s1);
		s.link(s2);
		
		ScopedObject so1 = new ScopedObject("x");
		s1.add(so1);
		ScopedObject so2 = new ScopedObject("x");
		s2.add(so2);
		
		assertEquals(so1, s1.find("x"));
		assertEquals(so2, s2.find("x"));
		
		try {
			s.find("x");
			fail();
		} catch (AmbiguousNameException e) {
			/*
			 * Expected.
			 */
		}
		
		ScopedObject so0 = new ScopedObject("x");
		s.add(so0);
		
		assertEquals(so1, s1.find("x"));
		assertEquals(so2, s2.find("x"));
		assertEquals(so0, s.find("x"));
	}
	
	@Test
	public void search_object_but_find_scope() throws Exception {
		TestScope s = new TestScope();
		s.add(new ScopedObject("x"));
		
		assertNull(s.find(new HierarchicalName(false, "x", "y")));
		assertNull(s.find_scope(new HierarchicalName(false, "x", "y")));
	}
	
	@Test
	public void find_subscope() throws Exception {
		TestScope s = new TestScope();
		TestScope ss = new TestScope("s");
		s.add(ss);
		
		assertEquals(ss, s.find_scope("s"));
		assertNull(s.find_scope("y"));
	}
	
	@Test
	public void list_scope_contents_directly_or_recursively() throws Exception {
		TestScope s = new TestScope();
		TestScope ss = new TestScope("ss");
		s.add(ss);
		ScopedObject s1 = new ScopedObject("x");
		s.add(s1);
		ScopedObject s2 = new ScopedObject("y");
		ss.add(s2);
		
		Set<ScopedObject> all = s.all();
		assertEquals(1, all.size());
		assertTrue(all.contains(s1));
		
		Set<ScopedObject> all_r = s.all_recursive();
		assertEquals(2, all_r.size());
		assertTrue(all_r.contains(s1));
		assertTrue(all_r.contains(s2));
		
		
		TestScope ls = new TestScope();
		ScopedObject s3 = new ScopedObject("z");
		ls.add(s3);
		ss.link(ls);
		
		Set<ScopedObject> all_lr = s.all_recursive();
		assertEquals(3, all_lr.size());
		assertTrue(all_lr.contains(s1));
		assertTrue(all_lr.contains(s2));
		assertTrue(all_lr.contains(s3));
		
		all = s.all();
		assertEquals(1, all.size());
		assertTrue(all.contains(s1));
		
		all = ss.all();
		assertEquals(1, all.size());
		assertTrue(all.contains(s2));
	}
	
	@Test
	public void obtaining_absolute_hierarchical_name() throws Exception {
		TestScope g = new TestScope();
		assertNull(g.absolute_hname());
		
		ScopedObject o1 = new ScopedObject("foo");
		g.add(o1);
		HierarchicalName o1_hn = o1.absolute_hname();
		assertTrue(o1_hn.absolute());
		assertEquals("::foo", o1_hn.toString());
		
		TestScope ss = new TestScope("ss");
		g.add(ss);
		HierarchicalName ss_hn = ss.absolute_hname();
		assertTrue(ss_hn.absolute());
		assertEquals("::ss", ss_hn.toString());
		
		ScopedObject o2 = new ScopedObject("bar");
		ss.add(o2);
		HierarchicalName o2_hn = o2.absolute_hname();
		assertTrue(o2_hn.absolute());
		assertEquals("::ss::bar", o2_hn.toString());
		
		TestScope sss = new TestScope("sss");
		ss.add(sss);
		HierarchicalName sss_hn = sss.absolute_hname();
		assertTrue(sss_hn.absolute());
		assertEquals("::ss::sss", sss_hn.toString());
	}
	
	@Test(expected = AssertionError.class)
	public void linking_same_scope_twice() throws Exception {
		TestScope s1 = new TestScope();
		TestScope l = new TestScope();
		
		s1.link(l);
		s1.link(l);
	}
	
	@Test
	public void check_object_not_added_to_any_scope_in_scope()
			throws Exception {
		ScopedObject x = new ScopedObject("x");
		assertFalse(x.in_scope(new TestScope()));
	}
	
	@Test
	public void check_object_in_its_own_scope() throws Exception {
		TestScope s0 = new TestScope();
		ScopedObject o0 = new ScopedObject("x");
		s0.add(o0);
		assertTrue(o0.in_scope(s0));
	}
	
	@Test
	public void check_object_in_its_parent_scope() throws Exception {
		TestScope s0 = new TestScope();
		TestScope s1 = new TestScope("s");
		s0.add(s1);
		ScopedObject o1 = new ScopedObject("x");
		s1.add(o1);
		assertTrue(o1.in_scope(s0));
	}
	
	@Test
	public void check_object_not_in_sub_scope() throws Exception {
		TestScope s0 = new TestScope();
		TestScope s1 = new TestScope("s");
		s0.add(s1);
		ScopedObject o0 = new ScopedObject("x");
		s0.add(o0);
		assertFalse(o0.in_scope(s1));
	}
	
	@Test
	public void self_child_check_test() throws Exception {
		TestScope s0 = new TestScope();
		assertFalse(s0.child_scope_of(s0));
	}
	
	@Test
	public void direct_child_check_test() throws Exception {
		TestScope s0 = new TestScope();
		TestScope s1 = new TestScope("s1");
		s0.add(s1);
		assertFalse(s0.child_scope_of(s1));
		assertTrue(s1.child_scope_of(s0));
	}
	
	@Test
	public void indirect_child_check_test() throws Exception {
		TestScope s0 = new TestScope();
		TestScope s1 = new TestScope("s1");
		s0.add(s1);
		TestScope s2 = new TestScope("s2");
		s1.add(s2);
		assertFalse(s0.child_scope_of(s2));
		assertTrue(s2.child_scope_of(s0));
	}
	
	@Test
	public void non_child_child_check_test() throws Exception {
		TestScope s0 = new TestScope();
		TestScope s1 = new TestScope("s1");
		s0.add(s1);
		TestScope s2 = new TestScope("s2");
		s0.add(s2);
		assertFalse(s1.child_scope_of(s2));
		assertFalse(s2.child_scope_of(s1));
	}
	
	@Test
	public void find_common_scope_single_scope() throws Exception {
		TestScope s0 = new TestScope();
		Set<Scope<?>> set = new HashSet<>();
		set.add(s0);
		Scope<?> found = Scope.common_inner_most_parent_scope(set);
		assertEquals(s0, found);
	}
	
	@Test
	public void find_common_scope_with_parent_and_child() throws Exception {
		TestScope s0 = new TestScope();
		TestScope s1 = new TestScope("x");
		s0.add(s1);
		Set<Scope<?>> set = new HashSet<>();
		set.add(s0);
		set.add(s1);
		Scope<?> found = Scope.common_inner_most_parent_scope(set);
		assertEquals(s0, found);
	}
	
	@Test
	public void find_common_scope_with_two_grandchildren()
			throws Exception {
		TestScope s0 = new TestScope();
		TestScope s1 = new TestScope("x");
		s0.add(s1);
		TestScope s2 = new TestScope("y");
		s0.add(s2);
		TestScope s3 = new TestScope("z");
		s1.add(s3);
		TestScope s4 = new TestScope("w");
		s2.add(s4);
		
		Set<Scope<?>> set = new HashSet<>();
		set.add(s3);
		set.add(s4);
		Scope<?> found = Scope.common_inner_most_parent_scope(set);
		assertEquals(s0, found);
	}
	
	@Test
	public void find_common_scope_no_common() throws Exception {
		TestScope s0 = new TestScope();
		TestScope s1 = new TestScope();
		Set<Scope<?>> set = new HashSet<>();
		set.add(s0);
		set.add(s1);
		Scope<?> found = Scope.common_inner_most_parent_scope(set);
		assertNull(found);
	}
}
