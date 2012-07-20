package info.shuiyue.jetris;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;


public class GameCanvas extends Canvas {

	private static final long serialVersionUID = 1L;
	public static final int WIDTH = 360;
	public static final int HEIGHT = 480;
	public static final int TILE_SIZE = 24;
	public static int speed = 300;
	private Image tileImg;
	private Image bgImg;
	private Image offImg;
	private Image gameOverImg;
	private Graphics offG;
	private int[][] tilePt;
	private int[][] tilePtSrc;
	private int x,y;
	private boolean hasOne;
	private int changeIndex = 0;
	private Object obj = new Object();
	private boolean gameOver = false;
	private int[][] map = new int[20][15];
	public long lastOperateTime = new Date().getTime();
	
	public GameCanvas(){
		tilePt = generateTiles();
		tilePtSrc =tilePt;
		loadImg();
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setFocusable(true);
		addKeyListener(new TileKeyListener());
		new Thread(new TileDownRunnable()).start();
	}
	
	private void loadImg() {
//		File bgFile = new File("D:/resources/1.jpg");
//		File tileFile = new File("D:/resources/2.jpg");
//		File gameOverFile = new File("D:/resources/gameover.jpg");
//		try {
//			tileImg = ImageIO.read(tileFile);
//			bgImg = ImageIO.read(bgFile);
//			gameOverImg = ImageIO.read(gameOverFile);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
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
	
	private void drawGameOver(Graphics g) {
		g.drawImage(gameOverImg, 0, 0, null);		
	}

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

	private void drawBG(Graphics g) {
		g.drawImage(bgImg, 0, 0, null);
	}

	private int[][] generateTiles(){
		x=6;
		y=0;
		int[][] pt = TileUtil.getInstance().generateTiles();
		return pt;
	}
	
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
	
	private int getTileHeight(int[][] pt) {
		return pt.length;
	}
	
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
				if(map[getTileHeight(tilePt)-1][x]==1){
					gameOver = true;
					try {
						drawScreen();
						Thread.sleep(4000);
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
//				System.out.println("1****"+tilePt.length);
				if(new Date().getTime()-lastOperateTime<speed){
					try {
						Thread.sleep(speed);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				synchronized (obj) {
					if(!checkCollision(tilePt,CommonUtil.DOWN)){
						y++;
					}else{
						changeMapStatus(tilePt);
					}
//					System.out.println("2****"+tilePt.length);
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
		clearMap();
		drawScreen();
		hasOne = false;
	}
	
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

	private class TileKeyListener implements KeyListener{

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();
			switch(keyCode){
				case KeyEvent.VK_LEFT :
				synchronized (obj) {
					if (!checkCollision(tilePt,CommonUtil.LEFT)) {
						x--;
						lastOperateTime = new Date().getTime();
					}
				}
				break;
				case KeyEvent.VK_RIGHT:
				synchronized (obj) {
					if (!checkCollision(tilePt,CommonUtil.RIGHT)) {
						x++;
						lastOperateTime = new Date().getTime();
					}
				}
				break;
				case KeyEvent.VK_UP:
					synchronized (obj) {
						changeTile();
					}				
					break;
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

		private void changeTile() {
			int[][] tmpTile;
			List<int[][]> tileList = TileUtil.getInstance().getTiles(tilePtSrc);
			int len = tileList.size();
			int index = changeIndex+1;
			if(index>len-1){
				tmpTile = tileList.get(0);
				if((x+getTileWidth(tmpTile))*TILE_SIZE  <= WIDTH 
						&& !checkCollision(tmpTile,CommonUtil.DOWN)){
					changeIndex = 0;
					tilePt = tmpTile;
				}
			}else{
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
