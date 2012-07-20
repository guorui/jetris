package info.shuiyue.jetris;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class TileUtil {
	private static final int[][] tile1 = { {1,1,1,1} };
	private static final int[][] tile2 = { {1}, {1,1,1} };
	private static final int[][] tile3 = { {0,1}, {1,1,1} };
	private static final int[][] tile4 = { {0,0,1}, {1,1,1} };
	private static final int[][] tile5 = { {1,1}, {1,1} };
	
	private static final int[][] tile11 = {{1}, {1}, {1}, {1}};
	
	private static final int[][] tile21 = {{1,1}, {1}, {1}};
	private static final int[][] tile22 = {{1,1,1}, {0,0,1}};
	private static final int[][] tile23 = {{0,1}, {0,1}, {1,1}};
	
	private static final int[][] tile31 = {{1}, {1,1}, {1}};
	private static final int[][] tile32 = {{1,1,1}, {0,1}};
	private static final int[][] tile33 = {{0,1}, {1,1}, {0,1}};
	
	private static final int[][] tile41 = {{1}, {1}, {1,1}};
	private static final int[][] tile42 = {{1,1,1}, {1}};
	private static final int[][] tile43 = {{1,1}, {0,1}, {0,1}};
	
	private List<int[][]> list1 = new ArrayList<int[][]>();
	private List<int[][]> list2 = new ArrayList<int[][]>();
	private List<int[][]> list3 = new ArrayList<int[][]>();
	private List<int[][]> list4 = new ArrayList<int[][]>();
	private List<int[][]> list5 = new ArrayList<int[][]>();
	
	private  List<int[][]> tiles;
	private Map<int[][],List<int[][]>> map;
	private static TileUtil instance;
	private TileUtil(){
		tiles = new ArrayList<int[][]>();
		tiles.add(tile1);
		tiles.add(tile2);
		tiles.add(tile3);
		tiles.add(tile4);
		tiles.add(tile5);
		
		map = new HashMap<int[][],List<int[][]>>();
		map.put(tile1, list1);
		map.put(tile2, list2);
		map.put(tile3, list3);
		map.put(tile4, list4);
		map.put(tile5, list5);
		
		list1.add(tile1);
		list1.add(tile11);
	
		list2.add(tile2);
		list2.add(tile21);
		list2.add(tile22);
		list2.add(tile23);
		
		list3.add(tile3);
		list3.add(tile31);
		list3.add(tile32);
		list3.add(tile33);
		
		list4.add(tile4);
		list4.add(tile41);
		list4.add(tile42);
		list4.add(tile43);
		
		list5.add(tile5);
	}
	
	public static TileUtil getInstance(){
		if(instance == null){
			instance = new TileUtil();
		}
		return instance;
	}
	
	public int[][] generateTiles(){
		return tiles.get(new Random().nextInt(5));
	}
	
	public List<int[][]> getTiles(int[][] tile){
		return map.get(tile);
	}
}
