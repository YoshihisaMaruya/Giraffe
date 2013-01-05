#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/highgui/highgui.hpp>
//for OpenCV 2.4.x (2.3.x系の場合はいらない)
#include <opencv2/legacy/legacy.hpp>
#include <opencv2/nonfree/nonfree.hpp>
#include <opencv2/gpu/gpu.hpp>

#include <stdio.h>
#include <string>
#include <vector>

using namespace cv;
using namespace std;

//各スレッド用に保存場所を用意
float** descriptors_holder;
int* rows_holder;
int* cols_holder;
int* keypoints_size_holder;
int thread_num;

void exeSurfFromMat(const int id, const Mat img) {
	SurfFeatureDetector surf_detector; //SURF特徴点検出器 TODO: 引数について: http://opencv.jp/opencv-2.2/c/features2d_feature_detection_and_description.html
	vector < KeyPoint > trainKeypoints;
	Mat gray(img.rows, img.cols, CV_8UC1); //グレーイメジに変換
	cvtColor(img, gray, CV_RGBA2GRAY, 0);
	normalize(gray, gray, 0, 255, NORM_MINMAX);

	//lshのために、keypointのサイズを保存
	surf_detector.detect(gray, trainKeypoints);
	keypoints_size_holder[id] = trainKeypoints.size();

	// SURFに基づくディスクリプタ抽出器
	SurfDescriptorExtractor surf_extractor; //SURF特徴量抽出機
	Mat trainDescriptors;
	surf_extractor.compute(gray, trainKeypoints, trainDescriptors);
	cols_holder[id] = trainDescriptors.cols;
	rows_holder[id] = trainDescriptors.rows;
	int col = trainDescriptors.cols;
	int row = trainDescriptors.rows;

	//TODO: 一旦freeする必要がるかも
	float* descriptors = descriptors_holder[id];
	if (descriptors_holder[id] != NULL) {
		delete descriptors_holder[id];
	}

	descriptors_holder[id] = new float[col * row];

	int count = 0;
	for (int i = 0; i < row; i++) {
		for (int j = 0; j < col; j++) {
			descriptors_holder[id][count] = trainDescriptors.at<float>(i, j);
			count++;
		}
	}
}

extern "C" {

//各スレッド用の入れ物を用意
void init(int tn) {
	thread_num = tn;
	descriptors_holder = new float*[thread_num];
	for (int i = 0; i < thread_num; i++) {
		descriptors_holder[i] = NULL;
	}
	rows_holder = new int[thread_num];
	cols_holder = new int[thread_num];
	keypoints_size_holder = new int[thread_num];
}

//リソースの開放
void freeAll() {
    delete rows_holder;
    delete cols_holder;
    delete keypoints_size_holder;
    for(int i = 0; i < thread_num; i++){
        delete descriptors_holder[i];
    }
    delete descriptors_holder;
}

int getCol(int id) {
	return cols_holder[id];
}

int getRow(int id) {
	return rows_holder[id];
}

int getKeypointsSize(int id) {
	return keypoints_size_holder[id];
}

float* getDescriptors(int id) {
	return descriptors_holder[id];
}

//ImageファイルからSURF特徴量を取得
int exeSurfFromFile(int id, char* file_path) {
	cv::Mat img = cv::imread(file_path);
	exeSurfFromMat(id, img);
}

void exeSurfFromRgb(int id,int width,int height,unsigned char * rgb){
	Mat img(height,width,CV_8UC4,rgb);

	//COLOR_mRGBA2RGBA
	Mat dst(height,width,CV_8UC4);
	cvtColor(img, dst, CV_BGRA2RGBA);
	imwrite("/Users/maruya/Desktop/hoge/CV_BGRA2RGBA.bmp",dst);

	exeSurfFromMat(id,dst);
}

//yuvからSURF特徴量を検出
int exeSurfFromYuv(int id, int width, int height, unsigned char * yuv) {
	Mat myuv(height + height / 2, width, CV_8UC1, yuv);
	exeSurfFromMat(id, myuv);

	/*
	 Mat gray(height, width, CV_8UC1, yuv);

	 SurfFeatureDetector surf_detector; //SURF特徴点検出器 TODO: 引数について: http://opencv.jp/opencv-2.2/c/features2d_feature_detection_and_description.html
	 vector<KeyPoint> trainKeypoints;

	 normalize(gray, gray, 0, 255, NORM_MINMAX); //要らない??
	 surf_detector.detect(gray,trainKeypoints);
	 //lshのために、keypointのサイズを保存
	 keypoints_size_holder[id] = trainKeypoints.size();
	 // SURFに基づくディスクリプタ抽出器
	 SurfDescriptorExtractor surf_extractor; //SURF特徴量抽出機
	 Mat trainDescriptors;
	 surf_extractor.compute(gray,trainKeypoints,trainDescriptors);
	 cols_holder[id] = trainDescriptors.cols;
	 rows_holder[id] = trainDescriptors.rows;
	 int cols = trainDescriptors.cols;
	 int rows = trainDescriptors.rows;
	 descriptors_holder[id] = new float[cols * rows]; //TODO: 一旦freeする必要がるかも
	 int count = 0;
	 for(int i = 0; i < rows; i++){
	 for(int j = 0; j < cols; j++){
	 descriptors_holder[id][count] = trainDescriptors.at<float>(i,j);
	 count++;
	 }
	 }*/
}
}
