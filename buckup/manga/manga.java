package manga;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class manga {
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) {

		String path_in = "C:/Users/Tonegawa/Pictures/simple.png";
		String path_gray_out = "C:/Users/Tonegawa/Pictures/manga/sample_gray.jpg";
		String path_edge_out = "C:/Users/Tonegawa/Pictures/manga/sample_edges.jpg";
		String path_white_out = "C:/Users/Tonegawa/Pictures/manga/white_out.jpg";
		String path_sample_out = "C:/Users/Tonegawa/Pictures/manga/sample_out.jpg";
		String path_white_endline_out = "C:/Users/Tonegawa/Pictures/manga/white_endline_out.jpg";
		String path_Rect = "C:/Users/Tonegawa/Pictures/manga/Rect.jpg";
		String path_in_drown = "C:/Users/Tonegawa/Pictures/Findcontours/sample_rect.jpg";

		Mat mat_src = new Mat();
		Mat gray = new Mat();
		Mat edges = new Mat();
		Mat lines = new Mat();
		Mat white = new Mat();
		Mat white2 = new Mat();
		Mat rect = new Mat();
		rect = Highgui.imread(path_in_drown);
		double[][] contours;

		//debug
		Highgui.imwrite(path_Rect, rect);

		mat_src = Highgui.imread(path_in);						 // 入力画像の読み込み
		white = mat_src.clone();
		white.setTo(new Scalar(255, 255, 255));
		white2 = white.clone();
		//rect = white.clone();

		Imgproc.cvtColor(mat_src, gray, Imgproc.COLOR_BGR2GRAY); // カラー画像をグレー画像に変換
		Imgproc.Canny(gray, edges, 100, 200);					//　エッジ検出
		Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180, 100, 100, 5);	//直線検出
		fncDrwLine(lines, white);									//直線描画
		contours = fncDrwLine(lines, mat_src);

		komaDefine(mat_src, contours);

		int[] size = endLineSize(mat_src, contours);
		double[][] x0EndLine =  findLineX0End(mat_src, contours);
		double[][] y0EndLine = findLineY0End(mat_src, contours);
		double[][] xMaxEndLine = findLineXMAXEnd(mat_src, contours);
		double[][] yMaxEndLine = findLineYMAXEnd(mat_src, contours);			//各辺の端点情報

		//double[][] end_line = findLineEnd(mat_src, contours, x0EndLine, xMaxEndLine, y0EndLine, yMaxEndLine);
		drwLineEnd(white2, x0EndLine, xMaxEndLine, y0EndLine, yMaxEndLine);

		cutEndLineRect(rect ,mat_src, x0EndLine, xMaxEndLine, y0EndLine, yMaxEndLine);

		Highgui.imwrite(path_gray_out, gray);						// 出力画像を保存
		Highgui.imwrite(path_edge_out, edges);
		Highgui.imwrite(path_white_out, white);
		Highgui.imwrite(path_white_endline_out, white2);
		Highgui.imwrite(path_sample_out, mat_src);
	}

	private static double[][] fncDrwLine(Mat line,Mat img) {
		double[] data;
		double[][] contours = new double[line.cols()][4];
 		Point pt1 = new Point();
		Point pt2 = new Point();
		int count = 0;
		System.out.println(line.cols());
		for (int i = 0; i < line.cols(); i++){
			data = line.get(0, i);
			pt1.x = data[0];

			pt1.y = data[1];

			pt2.x = data[2];

			pt2.y = data[3];

			if(pt1.x == pt2.x || pt1.y == pt2.y){
				Core.line(img, pt1, pt2, new Scalar(255,0,0), 1);
				contours[count][0]  = pt1.x;
				contours[count][1]  = pt1.y;
				contours[count][2]  = pt2.x;
				contours[count][3]  = pt2.y;
				//
				System.out.print("(" + contours[count][0] + ",");
				System.out.print(contours[count][1] + "), ");
				System.out.print("(" + contours[count][2] + ",");
				System.out.println(contours[count][3] + ")");
				count++;
			}
		}
		System.out.println(contours.length);
		System.out.println(count);
		System.out.println();
		double[][] returnContours = new double[count][4];

		for(int i = 0;i < count;i++){
			for(int j = 0;j < 4;j++){
				returnContours[i][j] = contours[i][j];
			}
		}

		for(int i = 0;i < count;i++){
			System.out.print("(" + returnContours[i][0] + ",");
			System.out.print(returnContours[i][1] + "), ");
			System.out.print("(" + returnContours[i][2] + ",");
			System.out.println(returnContours[i][3] + ")");
		}

		return returnContours;
	}


	//線分検出手法
	//バグあり
	//使わないかも
	private static void komaDefine(Mat src, double[][] point){
		System.out.println("komaDefine open");
		Koma[] koma = new Koma[10];
		int xmax = src.width();
		int count = 0;
		boolean x0Frag = false, xmaxFrag = false, y0Frag = false, ymaxFrag = false;

		for(int i = 0;i < point.length;i++){
			if(point[i][0] == 0 || point[i][2] == xmax){
				if(point[i][0] == 0) x0Frag = true;
				if(point[i][2] == xmax) xmaxFrag = true;
				for(int j = i+1;j < point.length;j++){
					if(x0Frag){
						//左下
						if(point[i][2] >= point[j][0] - 10 && point[i][2] <= 10 + point[j][0] && point[i][3] <= 10 + point[j][1] && point[i][3] >= point[j][1] - 10){
							koma[count] = new Koma(src, new Point(point[j][2], point[j][3]), new Point(point[i][0], point[i][1]));
							System.out.println("save succece 左下　　count = " + count);
							System.out.print(koma[count].getMinPoint() + " ");
							System.out.println(koma[count].getMaxPoint());
							count++;
						}
						//左上
						if(point[i][2] == point[j][2] && point[i][3] == point[j][3]){
							koma[count] = new Koma(src, new Point(point[i][2], point[i][3]), new Point(point[i][0], point[j][1]));
							System.out.println("save succece 左上　　count = " + count);
							count++;
						}
						x0Frag = false;
					}

					if(xmaxFrag){
						//右上
						if(point[i][0] == point[j][2] && point[i][1] == point[j][3]){
							koma[count] = new Koma(src, new Point(point[i][2], point[i][3]), new Point(point[j][0], point[j][1]));
							System.out.println("save succece 右上　　count = " + count);
							count++;
						}
						//右下
						if(point[i][0] == point[j][0] && point[i][1] == point[j][1]){
							koma[count] = new Koma(src, new Point(point[i][2], point[j][3]), new Point(point[i][0], point[i][1]));
							System.out.println("save succece 右下　　count = " + count);
							count++;
						}
						xmaxFrag = false;
					}

				}
			}
		}

	}

	//線分の本数を数えるメソッド
	public static int[] endLineSize(Mat src, double contours[][]){
		int[] size = {0, 0, 0, 0};
		int XMAX = src.width();
		int YMAX = src.height();

		for(int i = 0;i  < contours.length;i++){
			if(contours[i][0] <= 5){
				size[0]++;
			}
			if(contours[i][3] <= 5){
				size[1]++;
			}
			if(contours[i][2] >= XMAX-5 ){
				size[2]++;
			}
			if(YMAX-5 <= contours[i][1]){
				size[3]++;
			}
		}

		return size;
	}

	//線分を一つの二次元配列に保存するためのメソッド
	//現状必要無い
	public static double[][] findLineEnd(Mat src, double contours[][]){
		double[][] endPoint = new double[15][4];
		int XMAX = src.width();
		int YMAX = src.height();
		int count = 0;

		for(int i = 0;i  < contours.length;i++){
			if(contours[i][0] <= 5 || contours[i][3] <= 5 || contours[i][2] >= XMAX-5 || YMAX-5 <= contours[i][1]){
				for(int j = 0;j < 4;j++){
					endPoint[count][j] = contours[i][j];
				}
				count++;
			}
		}

		System.out.println(count);

		return endPoint;
	}

	//1各辺の線分情報を保存するためのメソッド
	// 10/27
	// 線分の間隔が狭いとき保存しないための工夫が必要
	// ひとつひとつに分ける必要あり？
	// 10/28
	// わけたものを後述
	/*
	public static double[][] findLineEnd(Mat src, double contours[][],double[][] x0EndPoint, double[][] xMaxEndPoint, double[][] y0EndPoint, double[][] yMaxEndPoint){
		double[][] endPoint = new double[20][4];

		int[] size = endLineSize(src, contours);
		double[][] x0EndPointfull = new double[size[0]][4];
		double[][] y0EndPointfull = new double[size[1]][4];
		double[][] xMaxEndPointfull = new double[size[2]][4];
		double[][] yMaxEndPointfull = new double[size[3]][4];

		int XMAX = src.width();
		int YMAX = src.height();
		int x0count = 0, xMaxcount = 0, y0count = 0, yMaxcount = 0;
		int count = 0;

		//debug
		System.out.println(x0EndPoint.length);
		System.out.println(y0EndPoint.length);
		System.out.println(xMaxEndPoint.length);
		System.out.println(yMaxEndPoint.length);
		System.out.println(contours.length);

		for(int i = 0;i  < contours.length;i++){
			if(contours[i][0] <= 5 || contours[i][3] <= 5 || contours[i][2] >= XMAX-5 || YMAX-5 <= contours[i][1]){
				System.out.println(count);

				//x0部
				if(contours[i][0] <= 5){
					//debug
					System.out.println("x0count = " + x0count);
					System.out.println(x0EndPoint.length);
					//線分がかぶっているかどうかの処理
					//よさげ
					boolean x0flag = false;
					for(int k = 0;k < x0count;k++){
						//かぶっているかどうか
						x0flag = (contours[i][1] <= x0EndPointfull[k][1] + 5 && contours[i][1] >= x0EndPointfull[k][1] - 5);
						//debug
						System.out.println("if  = "  + x0flag);
						System.out.println("contours[i][1]  = " + contours[i][1] + " x0EndPointfull[k][1] = " + x0EndPointfull[k][1]);
						if(x0flag) break;
					}
					//かぶっていない場合の処理
					//追加する
					if(!x0flag){
						for(int j = 0;j < 4;j++){
							x0EndPointfull[x0count][j] = contours[i][j];
						}
						x0count++;
					}
				}

				//y0
				if(contours[i][3] <= 5){
					System.out.println("y0count = " + y0count);
					//線分がかぶっているかどうかの処理
					//よさげ
					boolean y0flag = false;
					for(int k = 0;k < y0count;k++){
						//かぶっているかどうか
						y0flag = (contours[i][0] <= y0EndPointfull[k][0] + 5 && contours[i][0] >= y0EndPointfull[k][0] - 5);
						//debug
						System.out.println("if  = "  + y0flag);
						System.out.println("contours[i][0]  = " + contours[i][0] + " x0EndPointfull[k][0] = " + y0EndPointfull[k][0]);
						if(y0flag) break;
					}
					//かぶっていない場合の処理
					//追加する
					if(!y0flag){
						for(int j = 0;j < 4;j++){
							y0EndPointfull[y0count][j] = contours[i][j];
						}
						y0count++;
					}
				}

				//xMax
				if(contours[i][2] >= XMAX-5 ){
					System.out.println("xMaxcount = " + xMaxcount);
					//線分がかぶっているかどうかの処理
					//よさげ
					boolean xMaxflag = false;
					for(int k = 0;k < xMaxcount;k++){
						//かぶっているかどうか
						xMaxflag = (contours[i][1] <= xMaxEndPointfull[k][1] + 5 && contours[i][1] >= xMaxEndPointfull[k][1] - 5);
						//debug
						System.out.println("if  = "  + xMaxflag);
						System.out.println("contours[i][1]  = " + contours[i][1] + " x0EndPointfull[k][1] = " + xMaxEndPointfull[k][1]);
						if(xMaxflag) break;
					}
					//かぶっていない場合の処理
					//追加する
					if(!xMaxflag){
						for(int j = 0;j < 4;j++){
							xMaxEndPointfull[xMaxcount][j] = contours[i][j];
						}
						xMaxcount++;
					}
				}

				//yMax
				if(YMAX-5 <= contours[i][1]){
					System.out.println("yMaxcount = " + yMaxcount);
					//線分がかぶっているかどうかの処理
					//よさげ
					boolean yMaxflag = false;
					for(int k = 0;k < yMaxcount;k++){
						//かぶっているかどうか
						yMaxflag = (contours[i][0] <= yMaxEndPointfull[k][0] + 5 && contours[i][0] >= yMaxEndPointfull[k][0] - 5);
						//debug
						System.out.println("if  = "  + yMaxflag);
						System.out.println("contours[i][0] = " + contours[i][0] + " x0EndPointfull[k][0] = " + yMaxEndPointfull[k][0]);
						if(yMaxflag) break;
					}
					//かぶっていない場合の処理
					//追加する
					if(!yMaxflag){
						for(int j = 0;j < 4;j++){
							yMaxEndPointfull[yMaxcount][j] = contours[i][j];
						}
						yMaxcount++;
					}
				}

				for(int j = 0;j < 4;j++){
					endPoint[count][j] = contours[i][j];
				}
				count++;
			}
		}

		x0EndPoint = new double[x0count][4];
		for(int i = 0;i < x0count;i++){
			x0EndPoint[i] = x0EndPointfull[i];
		}

		System.out.println("x0endPoin.length = " + x0EndPoint.length);

		y0EndPoint = new double[y0count][4];
		for(int i = 0;i < y0count;i++){
			x0EndPoint[i] = y0EndPointfull[i];
		}

		yMaxEndPoint = new double[yMaxcount][4];
		for(int i = 0;i < yMaxcount;i++){
			yMaxEndPoint[i] = yMaxEndPointfull[i];
		}

		xMaxEndPoint = new double[xMaxcount][4];
		for(int i = 0;i < xMaxcount;i++){
			xMaxEndPoint[i] = xMaxEndPointfull[i];
		}

		System.out.println(count);

		return endPoint;
	}
	*/

	public static double[][] findLineX0End(Mat src, double contours[][]){
		int[] size = endLineSize(src, contours);
		double[][] x0EndPointfull = new double[size[0]][4];

		int XMAX = src.width();
		int YMAX = src.height();
		int x0count = 0;

		for(int i = 0;i  < contours.length;i++){
			//x0部
			if(contours[i][0] <= 5){
				//線分がかぶっているかどうかの処理
				//よさげ
				boolean x0flag = false;
				for(int k = 0;k < x0count;k++){
					//かぶっているかどうか
					x0flag = (contours[i][1] <= x0EndPointfull[k][1] + 5 && contours[i][1] >= x0EndPointfull[k][1] - 5);
					if(x0flag) break;
				}
				//かぶっていない場合の処理
				//追加する
				if(!x0flag){
					for(int j = 0;j < 4;j++){
						x0EndPointfull[x0count][j] = contours[i][j];
					}
					x0count++;
				}
			}
		}

		double[][] x0EndPoint = new double[x0count][4];
		for(int i = 0;i < x0count;i++){
			x0EndPoint[i] = x0EndPointfull[i];
		}

		return x0EndPoint;
	}

	public static double[][] findLineY0End(Mat src, double contours[][]){
		int[] size = endLineSize(src, contours);
		double[][] y0EndPointfull = new double[size[1]][4];

		int XMAX = src.width();
		int YMAX = src.height();
		int y0count = 0;

		for(int i = 0;i  < contours.length;i++){
			//y0
			if(contours[i][3] <= 5){
				//線分がかぶっているかどうかの処理
				//よさげ
				boolean y0flag = false;
				for(int k = 0;k < y0count;k++){
					//かぶっているかどうか
					y0flag = (contours[i][0] <= y0EndPointfull[k][0] + 5 && contours[i][0] >= y0EndPointfull[k][0] - 5);
					if(y0flag) break;
				}
				//かぶっていない場合の処理
				//追加する
				if(!y0flag){
					for(int j = 0;j < 4;j++){
						y0EndPointfull[y0count][j] = contours[i][j];
					}
					y0count++;
				}
			}
		}

		double[][] y0EndPoint = new double[y0count][4];
		for(int i = 0;i < y0count;i++){
			y0EndPoint[i] = y0EndPointfull[i];
		}

		return y0EndPoint;
	}

	public static double[][] findLineXMAXEnd(Mat src, double contours[][]){
		int[] size = endLineSize(src, contours);
		double[][] xMaxEndPointfull = new double[size[2]][4];

		int XMAX = src.width();
		int YMAX = src.height();
		int xMaxcount = 0;

		for(int i = 0;i  < contours.length;i++){
			//xMax
			if(contours[i][2] >= XMAX-5 ){
				//線分がかぶっているかどうかの処理
				//よさげ
				boolean xMaxflag = false;
				for(int k = 0;k < xMaxcount;k++){
					//かぶっているかどうか
					xMaxflag = (contours[i][1] <= xMaxEndPointfull[k][1] + 5 && contours[i][1] >= xMaxEndPointfull[k][1] - 5);
					if(xMaxflag) break;
				}
				//かぶっていない場合の処理
				//追加する
				if(!xMaxflag){
					for(int j = 0;j < 4;j++){
						xMaxEndPointfull[xMaxcount][j] = contours[i][j];
					}
					xMaxcount++;
				}
			}
		}

		double[][] xMaxEndPoint = new double[xMaxcount][4];
		for(int i = 0;i < xMaxcount;i++){
			xMaxEndPoint[i] = xMaxEndPointfull[i];
		}

		return xMaxEndPoint;
	}

	public static double[][] findLineYMAXEnd(Mat src, double contours[][]){
		int[] size = endLineSize(src, contours);
		double[][] yMaxEndPointfull = new double[size[3]][4];

		int XMAX = src.width();
		int YMAX = src.height();
		int yMaxcount = 0;

		for(int i = 0;i  < contours.length;i++){
			//yMax
			if(YMAX-5 <= contours[i][1]){
				//線分がかぶっているかどうかの処理
				//よさげ
				boolean yMaxflag = false;
				for(int k = 0;k < yMaxcount;k++){
					//かぶっているかどうか
					yMaxflag = (contours[i][0] <= yMaxEndPointfull[k][0] + 5 && contours[i][0] >= yMaxEndPointfull[k][0] - 5);
					if(yMaxflag) break;
				}
				//かぶっていない場合の処理
				//追加する
				if(!yMaxflag){
					for(int j = 0;j < 4;j++){
						yMaxEndPointfull[yMaxcount][j] = contours[i][j];
					}
					yMaxcount++;
				}
			}
		}

		double[][] yMaxEndPoint = new double[yMaxcount][4];
		for(int i = 0;i < yMaxcount;i++){
			yMaxEndPoint[i] = yMaxEndPointfull[i];
		}

		return yMaxEndPoint;
	}

	//rappa-
	public static void cutEndLineRect(Mat src, double[][] x0EndPoint, double[][] xMaxEndPoint, double[][] y0EndPoint, double[][] yMaxEndPoint){
		int XMAX = src.width();
		int YMAX = src.height();
		double[] data = new double[3];
		double[] data2 = new double[3];
		boolean colorFlag = true;
		int colorCount = 0;

		Mat img_rect;

		Koma[] koma  =new Koma[10];

		Point left_up = new Point(0,0);
		Point left_down = new Point(0,YMAX);
		Point right_up = new Point(XMAX,0);
		Point right_down = new Point(XMAX,YMAX);

		// leftup
		// bug無し
		// このまま他のも実装
		String fileSpaceName = "LeftUP_Space";
		String filename;
		for(Integer i = 0;i < x0EndPoint.length;i++){
			for(Integer j = 0;j < y0EndPoint.length;j++){
				filename = fileSpaceName + i.toString() + j.toString();
				img_rect = fncCutImageRect(src, left_up, (int)y0EndPoint[j][0], (int)x0EndPoint[i][1]);

				//color find
				for(int h = 10;h < img_rect.rows() -10;h++){
					for(int k = 10;k < img_rect.cols() -10;k++){
						data = img_rect.get(h, k);

						if(data[0] >= 250 && data[1] == 0 && data[2] == 0){
							colorFlag = false;
							break;
						}
					}
					if(!colorFlag) break;
				}
				if(colorFlag)
					fncCutImageRect(src, left_up, (int)y0EndPoint[j][0], (int)x0EndPoint[i][1], filename);
				colorFlag = true;
			}
		}

		// leftdown
		// 線分検出に改善の余地あり
		fileSpaceName = "LeftDown_Space";
		Point left_down_start;
		colorFlag = true;

		//debug
		System.out.println(" yMaxEndPoint.length = " + yMaxEndPoint.length);
		System.out.println(" x0EndPoint.length = " + x0EndPoint.length);

		for(Integer i = 0;i < yMaxEndPoint.length;i++){
			for(Integer j = 0;j < x0EndPoint.length;j++){
				filename = fileSpaceName + i.toString() + j.toString();
				left_down_start = new Point(left_down.x, left_down.y  - x0EndPoint[j][1]);
				img_rect = fncCutImageRect(src, left_down_start, (int)yMaxEndPoint[i][0], (int)x0EndPoint[j][1]);

				//color find
				for(int h = 10;h < img_rect.rows()-10;h++){
					for(int k = 10;k < img_rect.cols()-10;k++){
						data = img_rect.get(h, k);

						//青色があるときの処理
						if(data[0] >= 250 && data[1] == 0 && data[2] == 0)
							colorCount++;
						if(colorCount == 1000){
							colorFlag = false;
							break;
						}

					}
					if(!colorFlag) break;
				}

				if(true){
					//debug
					System.out.println("left_down_start = " + left_down_start + " w = " + (int)yMaxEndPoint[i][0] + " h = " + (int)x0EndPoint[j][1]);
					fncCutImageRect(src, left_down_start, (int)yMaxEndPoint[i][0], (int)x0EndPoint[j][1], filename);
				}
				colorFlag = true;
			}
		}

	}

	public static void cutEndLineRect(Mat src, Mat img, double[][] x0EndPoint, double[][] xMaxEndPoint, double[][] y0EndPoint, double[][] yMaxEndPoint){
		int XMAX = src.width();
		int YMAX = src.height();
		double[] data = new double[3];
		double[] data2 = new double[3];
		boolean colorFlag = true;
		int colorCount  = 0;

		Mat img_rect;

		Koma[] koma  =new Koma[10];

		Point left_up = new Point(0,0);
		Point left_down = new Point(0,YMAX);
		Point right_up = new Point(XMAX,0);
		Point right_down = new Point(XMAX,YMAX);

		// leftup
		// bug無し
		// このまま他のも実装
		String fileSpaceName = "LeftUP_Space";
		String filename;
		for(Integer i = 0;i < x0EndPoint.length;i++){
			for(Integer j = 0;j < y0EndPoint.length;j++){
				filename = fileSpaceName + i.toString() + j.toString();
				img_rect = fncCutImageRect(src, left_up, (int)y0EndPoint[j][0], (int)x0EndPoint[i][1]);

				//color find
				for(int h = 10;h < img_rect.rows() -10;h++){
					for(int k = 10;k < img_rect.cols() -10;k++){
						data = img_rect.get(h, k);

						if(data[0] >= 250 && data[1] == 0 && data[2] == 0){
							colorFlag = false;
							break;
						}
					}
					if(!colorFlag) break;
				}
				if(colorFlag)
					fncCutImageRect(img, left_up, (int)y0EndPoint[j][0], (int)x0EndPoint[i][1], filename);
				colorFlag = true;
			}
		}

		// leftdown
		// 線分検出に改善の余地あり
		fileSpaceName = "LeftDown_Space";
		Point left_down_start;
		colorFlag = true;

		//debug
		System.out.println(" yMaxEndPoint.length = " + yMaxEndPoint.length);
		System.out.println(" x0EndPoint.length = " + x0EndPoint.length);
		System.out.println(" left_Down = " + left_down);
		//線分は正しく格納されている
		for(int i = 0;i <  x0EndPoint.length;i++){
			for(int j = 0;j < 4;j++){
				System.out.print(x0EndPoint[i][j] + " ");
			}
			System.out.println();
		}

		for(Integer i = 0;i < yMaxEndPoint.length;i++){
			for(Integer j = 0;j < x0EndPoint.length;j++){
				filename = fileSpaceName + i.toString() + j.toString();
				left_down_start = new Point(left_down.x, x0EndPoint[j][1]);
				img_rect = fncCutImageRect(src, left_down_start, (int)yMaxEndPoint[i][0], YMAX - (int)x0EndPoint[j][1]);

				//color find
				for(int h = 10;h < img_rect.rows()-10;h++){
					for(int k = 10;k < img_rect.cols()-10;k++){
						data = img_rect.get(h, k);
						data2 = img_rect.get(h+2, k+2);

						//青色があるときの処理
						if(data[0] >= 250 && data[1] == 0 && data[2] == 0)
							colorCount++;
						if(colorCount == 1000){
							colorFlag = false;
							break;
						}

					}
					if(!colorFlag) break;
				}

				if(true){
					//debug
					System.out.println("left_down_start = " + left_down_start + " w = " + (int)yMaxEndPoint[i][0] + " h = " + (int)x0EndPoint[j][1]);
					fncCutImageRect(img, left_down_start, (int)yMaxEndPoint[i][0], YMAX - (int)x0EndPoint[j][1], filename);
				}
				colorFlag = true;
			}
		}

	}

	//長方形切抜き
	private static Mat fncCutImageRect(Mat img, Point pt1, int w,int h,String filename){
		Rect roi = new Rect((int)pt1.x, (int)pt1.y, w, h);
		Mat img2 = new Mat(img, roi);
		String s = new String("C:/Users/Tonegawa/Pictures/manga/" + filename + ".jpg");
		Highgui.imwrite(s,img2);
		System.out.println(filename + ".jpg save success");
		return img2;
	}

	private static Mat fncCutImageRect(Mat img, Point pt1, int w,int h){
		Rect roi = new Rect((int)pt1.x, (int)pt1.y, w, h);
		Mat img2 = new Mat(img, roi);
		//String s = new String("C:/Users/Tonegawa/Pictures/manga/" + filename + ".jpg");
		//Highgui.imwrite(s,img2);
		//System.out.println(filename + ".jpg save success");
		return img2;
	}


	public static void drwLineEnd(Mat src, double[][] contours){
		int YMAX = src.height();
		int XMAX = src.width();

		for(int i = 0;i < contours.length;i++){
			if(contours[i][0] == contours[i][2]){
				Core.line(src, new Point(contours[i][0],0), new Point(contours[i][2], YMAX), new Scalar(255,0,0), 1);
			}
			if(contours[i][1] == contours[i][3]){
				Core.line(src, new Point(0,contours[i][1]), new Point(XMAX, contours[i][3]), new Scalar(255,0,0), 1);
			}
		}
	}

	public static void drwLineEnd(Mat src, double[][] x0EndPoint, double[][] xMaxEndPoint, double[][] y0EndPoint, double[][] yMaxEndPoint){
		int YMAX = src.height();
		int XMAX = src.width();

		System.out.println("x0EndPoint.length =  " + x0EndPoint.length);

		for(int i = 0;i < x0EndPoint.length;i++){
				Core.line(src, new Point(0, x0EndPoint[i][1]), new Point(XMAX,  x0EndPoint[i][3]), new Scalar(255,0,0), 1);
		}
		for(int i = 0;i < xMaxEndPoint.length;i++){
			Core.line(src, new Point(0, xMaxEndPoint[i][1]), new Point(XMAX,  xMaxEndPoint[i][3]), new Scalar(255,0,0), 1);
		}
		for(int i = 0;i < y0EndPoint.length;i++){
			Core.line(src, new Point(y0EndPoint[i][0],0), new Point(y0EndPoint[i][2], YMAX), new Scalar(255,0,0), 1);
		}
		for(int i = 0;i < yMaxEndPoint.length;i++){
			System.out.println();
			Core.line(src, new Point(yMaxEndPoint[i][0],0), new Point(yMaxEndPoint[i][0], YMAX), new Scalar(255,0,0), 1);
		}
	}


	//切り抜くコマの情報を一つにまとめたクラス
    public static class Koma{
        private Mat image;
        private Point max,min;
        private int high;
        private int width;

        //コンストラクタ
        public Koma(Mat src, Point getMax, Point getMin) {
            image = src;
            max = getMax;
            min = getMin;
            high = (int) (max.y - min.y);
            width = (int) (max.x - min.x);
        }

        public Point getMaxPoint() {
           return max;
       }
        public Point getMinPoint() {
           return min;
       }
        public int getMaxPointX() { return (int)max.x;}
        public int getMaxPointY() { return (int)max.y;}
        public int getMinPointX() { return (int)min.x;}
        public int getMinPointY() { return (int)min.y;}
        public int getHigh() {
            high = (int) (max.y - min.y);
            return high;
        }
        public int getWidth() {
            width = (int) (max.x - min.x);
            return width;
        }
        public Mat getImage(){
            return image;
        }

        public void setImage(Mat src) {
        	image = src;
        }
        public void setMaxPointX(int x) {
        	max.x = x;
        }
        public void setMinPointX(int x) {
           min.x = x;
        }
        public void setMaxPointY(int y) {
        	max.y = y;
        }
        public void setMinPointY(int y) {
        	min.y = y;
        }
        public void setMaxPoint(Point setMax){
        	max = setMax;
        }
        public void setMinPoint(Point setMin){
        	min = setMin;
        }
    }

}
