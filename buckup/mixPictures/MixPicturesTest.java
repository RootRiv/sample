package mixPictures;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

public class MixPicturesTest {
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) {
		String path_src1 = "C:/Users/Tonegawa/Pictures/Findcontours/sample_rect.jpg";
		String path_src2 = "C:/Users/Tonegawa/Pictures/manga/white_endline_out.jpg";
		String path_out = "C:/Users/Tonegawa/Pictures/MixPicture/line_and_rect.jpg";

		Mat src1 = new Mat();
		src1 = Highgui.imread(path_src1);
		Mat src2 = Highgui.imread(path_src2);
		Mat dst = src1.clone();

		Core.addWeighted(src1, 0.5, src2, 0.5, 0, dst);
		Highgui.imwrite(path_out, dst);

		
	}

}
