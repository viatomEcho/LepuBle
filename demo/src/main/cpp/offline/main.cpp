#include <iostream>
#include <cstdio>
#include "swt.h"
#include <deque>
#include <string>
#include <fstream>
#include "commalgorithm.h"
#include "streamswtqua.h"
#include <cmath>

using namespace std;
StreamSwtQua streamSwtQua;

void readfile(string path, deque <double> & data)
{
    ifstream infile(path.c_str());
    string tempstr;
    data.clear();


    while (getline(infile, tempstr))
    {
        data.push_back(StringToDouble(tempstr));
    }

    infile.close();
}

void writefile(string path, deque <double> & data)
{
    ofstream outfile(path.c_str());

    for (int i = 0; i < data.size(); ++i)
    {
        outfile << data[i] << endl;
    }
    outfile.close();
}


int main()
{
    deque <double > inputt;

    readfile("F:/1.txt",inputt);
    int i;
    deque <double> outputPoints;
    deque <double> allSig;
    deque <double> outputsize;
    int lenthOfData;
    int ReduntLength;
    int MultipleSize;
    lenthOfData = inputt.size();
    MultipleSize = lenthOfData/256;
    ReduntLength = lenthOfData - 256 * MultipleSize;

    if(ReduntLength != 0)
    {
        for(i = lenthOfData; i < (MultipleSize+1)*256; i++)
        {
            inputt.push_back(0);
        }
    }



   if(ReduntLength == 0)
   {
       for (i = 0; i < 256 * MultipleSize; ++i)
       {
           streamSwtQua.GetEcgData(inputt[i], outputPoints);

           for (int j = 0; j < outputPoints.size(); ++j)
           {
               allSig.push_back(outputPoints[j]);
           }
       }

   }
   else{
       for(i = 0; i < inputt.size(); i++)
       {
           streamSwtQua.GetEcgData(inputt[i], outputPoints);

           for (int j = 0; j < outputPoints.size(); ++j)
           {
               allSig.push_back(outputPoints[j]);
           }
       }
       if(ReduntLength < 192)
       {
           for(i = 0; i < 192 -ReduntLength; i++)
           {
               allSig.pop_back();
           }
       }

   }




    writefile("F:/10.txt", allSig);
    writefile("F:/size.txt",outputsize);
    std::cout << "Hello, World!" << std::endl;
    return 0;
}
