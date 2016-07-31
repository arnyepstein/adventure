package adv32;

import java.io.Serializable;

public class Item implements Serializable {
	public int id;
	public String name;
	public int props;
	public int location;
	public boolean isFixed;
	public boolean isTreasure;
}