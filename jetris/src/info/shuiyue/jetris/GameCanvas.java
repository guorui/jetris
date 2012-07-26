package info.shuiyue.jetris;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.List;


public class GameCanvas extends Canvas {

	private static final long serialVersionUID = 1L;
	public static final int WIDTH = 360;
	public static final int HEIGHT = 480;
	//每个小方块的大小
	public static final int TILE_SIZE = 24;
	//方块下落速度
	public static int speed = 300;
	//小方块图片
	private Image tileImg;
	//背景图片
	private Image bgImg;
	//图片缓冲
	private Image offImg;
	//游戏结束画面
	private Image gameOverImg;
	private Graphics offG;
	//当前正在下落的方块，变形后的
	private int[][] tilePt;
	//当前正在下落的方块，变形前的原始形状
	private int[][] tilePtSrc;
	//当前下落方块的坐标
	private int x,y;
	//屏幕有正在下落的方块
	private boolean hasOne;
	//代表一个方块可以变形的方块的索引
	private int changeIndex = 0;
	//同步使用
	private Object obj = new Object();
	//游戏结束标志
	private boolean gameOver = false;
	//背景地图
	private int[][] map = new int[20][15];
	//键盘操作的最后时间
	public long lastOperateTime = new Date().getTime();
	
	public GameCanvas(){
		//随机生成一种方块
		tilePt = generateTiles();
		//保存方块变形前的形状
		tilePtSrc =tilePt;
		//载入图片
		loadImg();
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setFocusable(true);
		//添加键盘操作监听
		addKeyListener(new TileKeyListener());
		//方块下落的线程
		new Thread(new TileDownRunnable()).start();
	}
	
	private void loadImg() {
		tileImg = CommonUtil.loadImage("img/2.jpg");
		bgImg = CommonUtil.loadImage("img/1.jpg");
		gameOverImg = CommonUtil.loadImage("img/gameover.jpg");
	}
	
	@Override
	public void update(Graphics g){
		paint(g);
	}
	
	@Override
	public void paint(Graphics g){
		if(offImg == null){
			offImg = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		}
		offG = offImg.getGraphics();
		if(gameOver){
			drawGameOver(offG);
		}else{
			drawBG(offG);
			drawTiles(offG);
			drawAllTile(offG);	
		}
		g.drawImage(offImg, 0, 0, null);
		g.dispose();
	}
	
	/**
	 * 绘制游戏结束的画面
	 * @param g
	 */
	private void drawGameOver(Graphics g) {
		g.drawImage(gameOverImg, 0, 0, null);		
	}
	
	/**
	 * 画出需要呈现的所有方块
	 */
	private void drawAllTile(Graphics g) {
		int len1 = map.length;
		for(int i=0; i<len1; i++){
			int len2 = map[i].length;
			for(int j=0; j<len2; j++){
				if(map[i][j]==1){
					g.drawImage(tileImg, j*TILE_SIZE, i*TILE_SIZE, null);
				}				
			}
		}		
	}
	
	/**
	 * 绘制随机生成的方块
	 * @param g
	 */
	private void drawTiles(Graphics g) {
		int len1 = tilePt.length;
		for(int i=0; i<len1; i++){
			int len2 = tilePt[i].length;
			for(int j=0; j<len2; j++){
				if(tilePt[i][j]==1){
					g.drawImage(tileImg, (x+j)*TILE_SIZE, (y+i)*TILE_SIZE, null);
				}				
			}
		}
	}
	
	/**
	 * 画背景
	 * @param g
	 */
	private void drawBG(Graphics g) {
		g.drawImage(bgImg, 0, 0, null);
	}
	
	/**
	 * 生成一种方块
	 * @return
	 */
	private int[][] generateTiles(){
		x=6;
		y=0;
		int[][] pt = TileUtil.getInstance().generateTiles();
		return pt;
	}
	
	/**
	 * 获取方块的宽度
	 * @param pt
	 * @return
	 */
	private int getTileWidth(int[][] pt) {
		int len1 = pt.length;
		int max = 0;
		for(int i=0; i<len1; i++){
			int len2 = pt[i].length;
			if(len2>max){
				max = len2;
			}
		}
		return max;
	}
	
	/**
	 * 获取方块的高度
	 * @param pt
	 * @return
	 */
	private int getTileHeight(int[][] pt) {
		return pt.length;
	}
	
	/**
	 * 方块下落的线程
	 * @author guorui
	 *
	 */
	private class TileDownRunnable implements Runnable{

		@Override
		public void run() {
			while(true){
				if(!hasOne){
					tilePt = generateTiles();
					tilePtSrc =tilePt;
					hasOne = true;
					changeIndex = 0;
				}
				
				//方块在最顶端时，结束游戏
				if(map[getTileHeight(tilePt)-1][x]==1){
					gameOver = true;
					try {
						drawScreen();
						Thread.sleep(5000);
						reset();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				try {
					Thread.sleep(speed);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//调节操作键盘时，方块下落速度问题
				if(new Date().getTime()-lastOperateTime<speed){
					try {
						Thread.sleep(speed);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				synchronized (obj) {
					//如果未碰撞，y加1
					if(!checkCollision(tilePt,CommonUtil.DOWN)){
						y++;
					}else{
						//改变地图状态
						changeMapStatus(tilePt);
					}

					drawScreen();					
				}		
			}
		}
		
	}
	
	/**
	 * 碰撞检测
	 * @param pt
	 * @param direction
	 * @return true表示发生碰撞
	 */
	private boolean checkCollision(int[][] pt, int direction) {
		boolean flag = false;
		int tileHeight = getTileHeight(pt);
		int tileWidth = getTileWidth(pt);
		
		if( (y+tileHeight)*TILE_SIZE  >=HEIGHT){
			flag = true;
			return flag;
		}
		
		int x1=0,y1=0,x2=0,y2 = 0;
		switch(direction){
			case  CommonUtil.LEFT :
				if(x <= 0 ){
					flag = true;
					return flag;
				}
				x1 = -1;
				y1 = 0;
				x2 = 0;
				y2 = -1;
				break;
			case  CommonUtil.RIGHT :
				if((x+tileWidth)*TILE_SIZE  >= WIDTH ){
					flag = true;
					return flag;
				}			
				x1 = 1;
				y1 = 0;
				x2 = 0;
				y2 = 1;
				
				break;
			case  CommonUtil.DOWN :
				x1 = 0;
				y1 = 1;
				x2 = 1;
				y2 = 0;
				break;
		}

		int len1 = pt.length;
		for(int i = 0; i<len1; i++){
			int len2 = pt[i].length;
			for(int j=0; j<len2; j++){
				
				if(!isSelf(j+x1, i+y1, pt) && map[y+x2+i][x+j+y2] == 1 && pt[i][j] == 1){
					flag = true;
//					printPt(pt);
//					System.out.println("2i="+i+",j="+j+",x="+x+",y="+y);
				}					
			}
		}
		return flag;
	}
	
	public void reset() {
		gameOver = false;
		tilePt = generateTiles();
		tilePtSrc =tilePt;
		map = new int[20][15];
		changeIndex = 0;
	}
	
	/**
	 * 测试使用，打印方块的坐标
	 * @param pt
	 */
	private void printPt(int[][] pt) {
		int len1 = pt.length;
		for(int i=0; i<len1; i++){
			int len2 = pt[i].length;
			StringBuffer sb = new StringBuffer();
			for(int j=0; j<len2; j++){
				sb.append(pt[i][j]+",");
			}
			System.out.println(sb);
		}
	}

	private boolean isSelf(int x, int y, int[][] pt) {
		boolean result = false;
		int len1 = pt.length;
		if(y>=len1 || x<0){
			return false;
		}
		int len2 = pt[y].length;
		if(x<len2 && pt[y][x]==1 ){
			result = true;
		}			
		return result;
	}
	
	/**
	 * 改变背景的状态，显示停止下落的方块，清除满格的行
	 * @param pt
	 */
	private void changeMapStatus(int[][] pt){
		int len1 = pt.length;
		for(int i = 0; i<len1; i++){
			int len2 = pt[i].length;
			for(int j=0; j<len2; j++){
				if(pt[i][j]==1){
					map[y+i][x+j]=1;					
				}
			}
		}	
		//清除方块满格的行
		clearMap();
		drawScreen();
		hasOne = false;
	}
	
	/**
	 * 清除方块满一行的行
	 */
	private void clearMap() {
		int mapLen1 = map.length;
		for(int i=0; i<mapLen1; i++){
			int mapLen2 = map[i].length;
			int sum = 0;
			for(int j=0; j<mapLen2; j++){
				sum+=map[i][j];
			}
			if(mapLen2 == sum){
				for(int j=0; j<mapLen2; j++){
					map[i][j] = 0;
				}
				
				if(i>0){
					for(int k=i-1; k>=0; k--){
						for(int j=0; j<mapLen2; j++){
							if(map[k][j]==1){
								map[k+1][j] = map[k][j];
								map[k][j]=0;
							}			
						}
					}					
				}
			}
		}
	}
	
	/**
	 * 键盘监听
	 *
	 */
	private class TileKeyListener implements KeyListener{

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();
			switch(keyCode){
				//向左移动
				case KeyEvent.VK_LEFT :
				synchronized (obj) {
					if (!checkCollision(tilePt,CommonUtil.LEFT)) {
						x--;
						lastOperateTime = new Date().getTime();
					}
				}
				break;
				//向右移动
				case KeyEvent.VK_RIGHT:
				synchronized (obj) {
					if (!checkCollision(tilePt,CommonUtil.RIGHT)) {
						x++;
						lastOperateTime = new Date().getTime();
					}
				}
				break;
				//变形
				case KeyEvent.VK_UP:
					synchronized (obj) {
						changeTile();
					}				
					break;
					//向下移动
				case KeyEvent.VK_DOWN:
				synchronized (obj) {
					if(checkCollision(tilePt,CommonUtil.DOWN)){
						hasOne = false;
					}else {
						y++;
					}
				}
				break;
			}
			drawScreen();
		}
		
		/**
		 * 方块变形，改变一种方块的形状
		 */
		private void changeTile() {
			int[][] tmpTile;
			//根据方块原始形状，获取可以变成的形状
			List<int[][]> tileList = TileUtil.getInstance().getTiles(tilePtSrc);
			int len = tileList.size();
			//当前形状的索引加1，表示下一个形状的索引
			int index = changeIndex+1;
			
			//索引超出范围，重置为原始的形状
			if(index>len-1){
				tmpTile = tileList.get(0);
				if((x+getTileWidth(tmpTile))*TILE_SIZE  <= WIDTH 
						&& !checkCollision(tmpTile,CommonUtil.DOWN)){
					changeIndex = 0;
					tilePt = tmpTile;
				}
			}
			//获取变形后的方块
			else{
				tmpTile = tileList.get(index);
				if((x+getTileWidth(tmpTile))*TILE_SIZE  <= WIDTH 
						&& !checkCollision(tmpTile,CommonUtil.DOWN)){
					changeIndex++;
					tilePt = tmpTile;
				}
			}
			
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private synchronized void drawScreen(){
		repaint();
	}
}
