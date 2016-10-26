package manga;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class Manga {
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) {

		String path_in = "C:/Users/meiji/Pictures/side_sample.jpg";
		String path_gray_out = "C:/Users/meiji/Pictures/manga/sample_gray.jpg";
		String path_edge_out = "C:/Users/meiji/Pictures/manga/sample_edges.jpg";
		String path_white_out = "C:/Users/meiji/Pictures/manga/white_out.jpg";
		String path_sample_out = "C:/Users/meiji/Pictures/manga/sample_out.jpg";
		String path_white_endline_out = "C:/Users/meiji/Pictures/manga/white_endline_out.jpg";
		String path_Rect = "C:/Users/meiji/Pictures/manga/Rect.jpg";
		String path_in_drown = "C:/Users/meiji/Pictures/sample_rect.jpg";
		
		

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
		

		mat_src = Highgui.imread(path_in);						 // ���͉摜�̓ǂݍ���
		white = mat_src.clone();
		white.setTo(new Scalar(255, 255, 255));
		white2 = white.clone();
		//rect = white.clone();

		Imgproc.cvtColor(mat_src, gray, Imgproc.COLOR_BGR2GRAY); // �J���[�摜���O���[�摜�ɕϊ�
		Imgproc.Canny(gray, edges, 100, 200);					//�@�G�b�W���o
		Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180, 100, 100, 5);	//�������o
		fncDrwLine(lines, white);									//�����`��
		contours = fncDrwLine(lines, mat_src);

		komaDefine(mat_src, contours);

		int[] size = endLineSize(mat_src, contours);
		double[][] x0EndLine = new double[size[0]][4];
		double[][] y0EndLine = new double[size[1]][4];
		double[][] xMaxEndLine = new double[size[2]][4];
		double[][] yMaxEndLine = new double[size[3]][4];			//�e�ӂ̒[�_���

		double[][] end_line = findLineEnd(mat_src, contours, x0EndLine, xMaxEndLine, y0EndLine, yMaxEndLine);
		drwLineEnd(white2, end_line);

		cutEndLineRect(rect, x0EndLine, xMaxEndLine, y0EndLine, yMaxEndLine);

		Highgui.imwrite(path_gray_out, gray);						// �o�͉摜��ۑ�
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


	//�������o��@
	//�o�O����
	//
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
						//����
						if(point[i][2] >= point[j][0] - 10 && point[i][2] <= 10 + point[j][0] && point[i][3] <= 10 + point[j][1] && point[i][3] >= point[j][1] - 10){
							koma[count] = new Koma(src, new Point(point[j][2], point[j][3]), new Point(point[i][0], point[i][1]));
							System.out.println("save succece �����@�@count = " + count);
							System.out.print(koma[count].getMinPoint() + " ");
							System.out.println(koma[count].getMaxPoint());
							count++;
						}
						//����
						if(point[i][2] == point[j][2] && point[i][3] == point[j][3]){
							koma[count] = new Koma(src, new Point(point[i][2], point[i][3]), new Point(point[i][0], point[j][1]));
							System.out.println("save succece ����@�@count = " + count);
							count++;
						}
						x0Frag = false;
					}

					if(xmaxFrag){
						//�E��
						if(point[i][0] == point[j][2] && point[i][1] == point[j][3]){
							koma[count] = new Koma(src, new Point(point[i][2], point[i][3]), new Point(point[j][0], point[j][1]));
							System.out.println("save succece �E��@�@count = " + count);
							count++;
						}
						//�E��
						if(point[i][0] == point[j][0] && point[i][1] == point[j][1]){
							koma[count] = new Koma(src, new Point(point[i][2], point[j][3]), new Point(point[i][0], point[i][1]));
							System.out.println("save succece �E���@�@count = " + count);
							count++;
						}
						xmaxFrag = false;
					}

				}
			}
		}

	}

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


	public static double[][] findLineEnd(Mat src, double contours[][],double[][] x0EndPoint, double[][] xMaxEndPoint, double[][] y0EndPoint, double[][] yMaxEndPoint){
		double[][] endPoint = new double[20][4];
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
				if(contours[i][0] <= 5){
					System.out.println("x0count = " + x0count);
					for(int j = 0;j < 4;j++){
						x0EndPoint[x0count][j] = contours[i][j];
					}
					x0count++;
				}
				if(contours[i][3] <= 5){
					System.out.println("y0count = " + y0count);
					for(int j = 0;j < 4;j++){
						y0EndPoint[y0count][j] = contours[i][j];
					}
					y0count++;
				}
				if(contours[i][2] >= XMAX-5 ){
					System.out.println("xMaxcount = " + xMaxcount);
					for(int j = 0;j < 4;j++){
						xMaxEndPoint[xMaxcount][j] = contours[i][j];
					}
					xMaxcount++;
				}
				if(YMAX-5 <= contours[i][1]){
					System.out.println("yMaxcount = " + yMaxcount);
					for(int j = 0;j < 4;j++){
						yMaxEndPoint[yMaxcount][j] = contours[i][j];
					}
					yMaxcount++;
				}
				for(int j = 0;j < 4;j++){
					endPoint[count][j] = contours[i][j];
				}
				count++;
			}
		}

		System.out.println(count);

		return endPoint;
	}

	public static void cutEndLineRect(Mat src, double[][] x0EndPoint, double[][] xMaxEndPoint, double[][] y0EndPoint, double[][] yMaxEndPoint){
		int XMAX = src.width();
		int YMAX = src.height();
		double[] data = new double[3];
		int row,col;
		boolean colorFlag = true;

		Mat img_rect;

		Koma[] koma  =new Koma[10];

		Point left_up = new Point(0,0);
		Point left_down = new Point(0,YMAX);
		Point right_up = new Point(XMAX,0);
		Point right_down = new Point(XMAX,YMAX);

		//leftup
		String fileSpaceName = "X0_Space";
		String filename;
		for(Integer i = 0;i < x0EndPoint.length;i++){
			for(Integer j = 0;j < y0EndPoint.length;j++){
				filename = fileSpaceName + i.toString() + j.toString();
				img_rect = fncCutImageRect(src, left_up, (int)y0EndPoint[j][0], (int)x0EndPoint[i][1]);
				//color find
				for(int h = 0;h < 2;h++){
					row = img_rect.rows()*(h+1) / 3;
					for(int k = 0;k < 2;k++){
						col = img_rect.cols()*(k+1) / 3;
						data = img_rect.get(row, col);
						
						System.out.println(i + ", " +  j);
						System.out.println("B " + data[0]);
						System.out.println("G " + data[1]);
						System.out.println("R " + data[2]);
						
						System.out.println("row = " + row + " col = " + col);
						if(data[0] >= 250 && data[1] == 0 && data[2] == 0){
							System.out.println("blue");
							colorFlag = false;
							fncCutImageRect(src, left_up, (int)y0EndPoint[j][0], (int)x0EndPoint[i][1], filename);
						}
					}
				}
			}
		}

		//leftdown

	}

	//�����`�ؔ���
	private static Mat fncCutImageRect(Mat img, Point pt1, int w,int h,String filename){
		Rect roi = new Rect((int)pt1.x, (int)pt1.y, w, h);
		Mat img2 = new Mat(img, roi);
		String s = new String("C:/Users/meiji/Pictures/manga/" + filename + ".jpg");
		Highgui.imwrite(s,img2);
		System.out.println(filename + ".jpg save success");
		return img2;
	}
	
	private static Mat fncCutImageRect(Mat img, Point pt1, int w,int h){
		Rect roi = new Rect((int)pt1.x, (int)pt1.y, w, h);
		Mat img2 = new Mat(img, roi);
		//String s = new String("C:/Users/meiji/Pictures/manga/" + filename + ".jpg");
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

	//�؂蔲���R�}�̏�����ɂ܂Ƃ߂��N���X
    public static class Koma{
        private Mat image;
        private Point max,min;
        private int high;
        private int width;

        //�R���X�g���N�^
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
