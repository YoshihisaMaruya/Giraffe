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

/*
void surf(const int id,const Mat img){
    float* n = new float[10];
    //char* file_path = argv[1];
    SurfFeatureDetector surf_detector; //SURF特徴点検出器 TODO: 引数について: http://opencv.jp/opencv-2.2/c/features2d_feature_detection_and_description.html
    vector<KeyPoint> trainKeypoints;
    Mat gray(img.rows,img.cols,CV_8UC1); //グレーイメジに変換
    cvtColor(img,gray,CV_RGBA2GRAY,0);
    normalize(gray, gray, 0, 255, NORM_MINMAX);
    surf_detector.detect(gray,trainKeypoints);
    // SURFに基づくディスクリプタ抽出器
    SurfDescriptorExtractor surf_extractor; //SURF特徴量抽出機
    Mat trainDescriptors;
    surf_extractor.compute(gray,trainKeypoints,trainDescriptors);
    cols[id] = trainDescriptors.cols;
    rows[id] = trainDescriptors.rows;
    int col = trainDescriptors.cols;
    int row = trainDescriptors.rows;
    array_descriptors[id] = new float[col * row];
    int count = 0;
    for(int i = 0; i < row; i++){
        for(int j = 0; j < col; j++){
            array_descriptors[id][count] = trainDescriptors.at<float>(i,j);
            count++;
        }
    }
}*/

extern "C" {
    
    void init(int thread_num){
        descriptors_holder = new float*[thread_num];
        rows_holder = new int[thread_num];
        cols_holder = new int[thread_num];
        keypoints_size_holder = new int[thread_num];
    }
    
    int getCols(int id){
        return cols_holder[id];
    }
    
    int getRows(int id){
        return rows_holder[id];
    }
    
    int getKeypointsSize(int id){
        return keypoints_size_holder[id];
    }
    
    float* getDescriptors(int id){
        return descriptors_holder[id];
    }
    
    /*
    int exeSurfFromFile(int id,char* file_path){
        cv::Mat img = cv::imread(file_path);
        surf(id,img);
    }*/
    
    
    
    //yubからSURF特徴量を検出
    int exeSurfFromYuv(int id, int width,int height,unsigned char * yuv){
        Mat myuv(height + height/2, width, CV_8UC1, yuv);
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
        descriptors_holder[id] = new float[cols * rows];
        int count = 0;
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < cols; j++){
                descriptors_holder[id][count] = trainDescriptors.at<float>(i,j);
                count++;
            }
        }
    }
}
