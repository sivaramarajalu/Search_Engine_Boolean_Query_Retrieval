import java.util.Comparator;
import java.util.Map;

class sortMapbyValue implements Comparator {
	 
	Map map;
 
	public sortMapbyValue(Map map) {
		this.map = map;
	}
 
	public int compare(Object keyA, Object keyB) {
		Comparable tempVar1 = (Comparable) map.get(keyA);
		Comparable tempVar2 = (Comparable) map.get(keyB);
		int i =tempVar2.compareTo(tempVar1);
		if(i==0)
			return -1; //returning zero merge keys
		else
			return i;
	}
}
