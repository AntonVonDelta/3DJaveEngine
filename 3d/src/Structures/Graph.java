package Structures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Graph<T> {
	private LinkedHashMap<T,LinkedHashSet<T>> memory=new LinkedHashMap<T, LinkedHashSet<T>>();
	
	public void addEdge(T start,T end) {
		// Also add the vertex components
		addVertex(start);
		addVertex(end);
		
		memory.get(start).add(end);
	}
	
	public void removeEdge(T start,T end) {
		if(!memory.containsKey(start)) return;
		memory.get(start).remove(end);
	}
	
	public void addVertex(T node) {
		if(!memory.containsKey(node)) {
			memory.put(node, new LinkedHashSet<>());
		}
	}
	
	public List<T> topologicalSort(){
		Set<T> global_visited=new HashSet<T>();
		List<T> order=new ArrayList<T>();
		
		while(global_visited.size()!=memory.size()) {
			for(T temp:memory.keySet()) {	
				if(!global_visited.contains(temp)) {
					recursive(temp, global_visited, new HashSet<T>(),order );
				}
			}
		}
		
		Collections.reverse(order);
		
		return order;
	}
	
	private void recursive(T current,Set<T> global_visited,Set<T> local_visited,List<T> order) {
		if(global_visited.contains(current)) {
			return;		// Already visited in a previous run
		}
		if(local_visited.contains(current)) {
			// Cycle detected!
			// Still we don't want to break the ordering.
			return;
		}
		
		// Mark current node
		local_visited.add(current);
		
		LinkedHashSet<T> adjacent=memory.get(current);
		for(T connected_node:adjacent) {
			recursive(connected_node,global_visited,local_visited,order);
		}
		
		local_visited.remove(current);
		global_visited.add(current);
		order.add(current);
	}
}
