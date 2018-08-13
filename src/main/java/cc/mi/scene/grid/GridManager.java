package cc.mi.scene.grid;

import cc.mi.scene.server.SceneMap;

public class GridManager {
	
	private final SceneMap map;
	private final int gridWidth ;
	private final int gridHeight;
	
	private final Grid[] grids;
	
	public GridManager(SceneMap map, int width, int height) {
		this(map, width, height, 15);
	}
	
	public GridManager(SceneMap map, int width, int height, int gridSides) {
		
		this.map = map;
		//初始化grid
		//gridSides个地图网格组成一个敏感区域网格
		//宽度方向上的敏感区域网格数量
		this.gridWidth  = (int) Math.ceil(1.0 * width  / gridSides);
		//高度方向上的敏感区域网格数量
		this.gridHeight = (int) Math.ceil(1.0 * height / gridSides);
		// 网格信息
		int size = this.gridWidth * this.gridHeight;
		this.grids = new Grid[size];

		int index = 0;
		for (int y = 0; y < this.gridHeight; ++ y) {
			for (int x = 0; x < this.gridWidth; ++ x) {
				int startX = 0;
				int startY = 0;
				int endX = width;
				int endY = height;
				//更新grid的逻辑坐标
				if (gridSides < 65535) {
					startX = gridSides * x;
					startY = gridSides * y;
					endX = startX + gridSides;
					endY = startY + gridSides;
				}
				Grid grid = new Grid(map, index, x, y, startX, startY, endX, endY);
				grid.setActive(false);
				
				this.grids[index] = grid;
				
				//将相关联的grid存下来,便于
				this.calcNoticeGrid(grid);
	
				index ++;
			}
		}
	}
	
	public Grid getGrid(int x, int y) {
		return this.grids[y*this.gridWidth+x];
	}
	
	// 计算grid附近的敏感grid
	private void calcNoticeGrid(Grid grid) {
		//最左上角的格子
		int sx = grid.getX() - 1;	
		int sy = grid.getY() - 1;

		for (int i = 0; i < 9; ++ i) {
			
			int ix = sx + i % 3;
			int iy = sy + i / 3;

			if (ix < 0 || iy < 0) {
				continue;
			}

			if (ix >= this.gridWidth || iy >= this.gridHeight) {
				continue;
			}
			grid.addGrid(this.getGrid(ix, iy));
		}
	}

	public SceneMap getMap() {
		return map;
	}

	public int getGridWidth() {
		return gridWidth;
	}

	public int getGridHeight() {
		return gridHeight;
	}
}
