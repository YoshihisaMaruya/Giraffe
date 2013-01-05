
#include <vector>
#include <string>
#include <fstream>

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

const double THRESHOLD = 0.5;

using namespace std;
using namespace cv;

////グローバル
CvLSH* lsh; //TODO: lshはグローバルに持たせて果たして大丈夫か？
vector<int> labels; //label(物体id所得のため)
int db_size = 0;

/*
 *vector<Mat> to CvMat*
 */
CvMat* VectorMatToCvMat(vector<Mat> mats) {
	int rows = 0;
	for (int i = 0; i < mats.size(); i++) {
		rows += mats[i].rows;
	}
	CvMat* objMat = cvCreateMat(rows, 128, CV_32FC1);
	int count = 0;
	for (int i = 0; i < mats.size(); i++) {
		for (int j = 0; j < mats[i].rows; j++) {
			for (int k = 0; k < mats[i].cols; k++) {
				float elem = mats[i].at<float>(j, k);
				CV_MAT_ELEM(*objMat,float,count,k) = elem;
			}
			count++;
		}
	}
	return objMat;
}

/*
 * foloat* to cvMat
 */
CvMat* createCvMatDescriptors(int rows, int cols, float* descriptors) {
	CvMat* cvMat = cvCreateMat(rows, cols, CV_32FC1);
	int count = 0;
	for (int i = 0; i < rows; i++) {
		for (int j = 0; j < cols; j++) {
			CV_MAT_ELEM(*cvMat,float,i,j) = descriptors[count];
			count++;
		}
	}
	return cvMat;
}

/*
 * idを取得
 */
int getObjId(int idx) {
	std::vector<int>::iterator label;
	int count = 0;
	int id = 0;
	//一つづつ特徴点DBのカラムを増やしていく
	for (label = labels.begin(); label != labels.end(); ++label) {
		count += *label;
		if (idx < count) {
			break;
		}
		id++;
	}
	return id;
}

/*
	 lshは投票機を独自に用意
	 @ return : id
 */
int* exe_vote(CvLSH* lsh, CvMat* indices, CvMat* dists) {
	vector<int> flag(LSHSize(lsh));
	//vector<int> votes(db_size);
	int* votes = new int[db_size + 1];

	//初期化
	for (int i = 0; i < db_size; i++) {
		votes[i] = 0;
	}

	for (int i = 0; i < indices->rows; i++) {
		int idx = CV_MAT_ELEM(*indices, int, i, 0);
		double dist = CV_MAT_ELEM(*dists, double, i, 0);
		if (idx < 0) {
			// can't find nn
			continue;
		}

		if (!flag[idx] && dist < THRESHOLD) {
			int id = getObjId(idx);
			votes[id]++;
			flag[idx] = 1;
		}
	}

	return votes;

	// 投票数が最大の物体IDを求める
	/*
	 int maxId = -1;
	 int maxVal = -1;
	 for (int i = 0; i < votes.size(); i++) {
	 if (votes[i] > maxVal) {
	 maxId = i;
	 maxVal = votes[i];
	 }
	 }
	 return maxId;*/
}

string IntToString(const int number) {
	stringstream ss;
	ss << number;
	return ss.str();
}

string CharsToString(const char* chars) {
	string ss = string(chars);
	return ss;
}

extern "C" {
void init(int dim,int n,int L,int k) {
	lsh = cvCreateMemoryLSH(dim, n, L, k, CV_32FC1); //lshの作成(メモリに領域確保)
}

//Mstデータから特徴量DBを作成
void readFromMstImg(char* dir_path, int count) {
	db_size = count;
	vector < Mat > descriptors;
	for (int i = 0; i < count; i++) {
		Mat training_image_mat = imread(
				CharsToString(dir_path) + "/" + IntToString(i) + ".jpg");
		//SURF特徴点検出器
		//TODO: 引数について: http://opencv.jp/opencv-2.2/c/features2d_feature_detection_and_description.html
		SurfFeatureDetector surf_detector;
		Mat gray(training_image_mat.rows, training_image_mat.cols, CV_8UC1); //グレーイメジに変換
		cvtColor(training_image_mat, gray, CV_RGBA2GRAY, 0);
		normalize(gray, gray, 0, 255, NORM_MINMAX);
		vector < KeyPoint > trainKeypoints;
		surf_detector.detect(gray, trainKeypoints);
		// SURFに基づくディスクリプタ抽出器
		SurfDescriptorExtractor surf_extractor; //SURF特徴量抽出機
		Mat descriptor;
		surf_extractor.compute(gray, trainKeypoints, descriptor);
		descriptors.push_back(descriptor);
		labels.push_back(descriptor.rows);
	}

	////サンプルのやり方
	CvMat* objMat = VectorMatToCvMat(descriptors);
	cvLSHAdd(lsh, objMat);
	cvReleaseMat(&objMat);
}

//LSHに追加
int add(int row, int col, float* descriptors){
	CvMat* insert = createCvMatDescriptors(row, col, descriptors);
	labels.push_back(row);
	cvLSHAdd(lsh, insert);
	cvReleaseMat(&insert);
	db_size++;
}
//マッチング
int* match(int query_keypoints_size,int row,int col, float* query_descriptors){
	int k = 1;  // k-NNのk
	CvMat* indices = cvCreateMat(query_keypoints_size, k, CV_32SC1);  // 1-NNのインデックス
	CvMat* dists = cvCreateMat(query_keypoints_size, k, CV_64FC1);     // その距離
	CvMat* queryMat = createCvMatDescriptors(row, col, query_descriptors);

	//マッチング実行
	cvLSHQuery(lsh, queryMat, indices, dists, k, 100);

	//投票実行
	int* result = exe_vote(lsh, indices, dists);

	cvReleaseMat(&queryMat);
	cvReleaseMat(&indices);
	cvReleaseMat(&dists);
	return result;
}
}
