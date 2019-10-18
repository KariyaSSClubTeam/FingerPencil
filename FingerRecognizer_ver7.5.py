import numpy as np
import cv2
import imutils

#動画読み込み
cap = cv2.VideoCapture("C:\\Users\\tuzuk\\Desktop\\TestMovie_10_03\\CIMG5910.MOV")
output = cv2.imread("C:\\Users\\tuzuk\\Desktop\\point_check\\range_2019-07-02.JPG")

width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
divided_width = int(width/30)
height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
divided_height = int(height/20)
first_line = int(height/15)
len_pointB_before = 0
#ここッ戦犯

fps = cap.get(cv2.CAP_PROP_FPS)

fourcc = cv2.VideoWriter_fourcc(*'DIVX')
writer = cv2.VideoWriter("C:\\Users\\tuzuk\\Desktop\\out.MOV", fourcc, fps, (width, height))

Old_cmp = 0
count = 0
pointA = []
pointB = []
pointB_fin = []

#一枚ずつフレームを取り出し処理を行う
while True:
    print('count',count)
    # 1フレームずつ取得する。
    ret, frame = cap.read()
    
      #取得した中で最大の輪郭を取得
    Area = []
    largestIndex = 0
    largestArea = 0
    def getLargestContoursAreaIndex(coutours):
        for i in range(0, len(contours)):
            Area.append(cv2.contourArea(contours[i]))
        
        largestIndex = np.argmax(Area)
        largestArea = max(Area)
        if largestArea < 15000:
           return None
            
        return largestIndex
       

    image = frame
    
    #フレームが空なら終了
    if frame is None:
        
        print('frame is None err in ',count)     
        
        count = count +1

        break
    
    
    #ぼかし処理
    gray = cv2.GaussianBlur(image, (15, 15), 0)
    
    #HSV変換
    hsv = cv2.cvtColor(gray, cv2.COLOR_BGR2HSV)


    # HSV空間で肌色の範囲を定義
    hsv_lower_blue = np.array([0,30,50])
    hsv_upper_blue = np.array([30,150,255])
    
    mask_hsv = cv2.inRange(hsv, hsv_lower_blue, hsv_upper_blue)
    
    #二値化画像を出力
    cv2.imwrite("C:\\Users\\tuzuk\\Desktop\\point_check\\" + str(count) +  ".JPG",mask_hsv)

    #全ての輪郭を取得
    contours_image, contours, hierarchy =  cv2.findContours(mask_hsv, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_NONE)

    #print(contours)

    
    #print(getLargestContoursAreaIndex(contours))
    #輪郭が取得できなかったとき表示
    if getLargestContoursAreaIndex(contours) is None:
        
        print('countors is None err in ',count)
        pointA.append(('',''))
        pointB.append(('',''))

        count = count +1
        
        continue
    

    #輪郭座標（配列）
   # cnt = getLargestContoursAreaIndex(contours)

    cnt = contours[getLargestContoursAreaIndex(contours)]

    #print([cnt])

    output_image = cv2.drawContours(image, [cnt], 0, (0, 0, 255), 1)
    #cv2.imwrite("C:\\Users\\tuzuk\\Desktop\\point_check\\" + str(count) +  ".JPG",output_image)
    
    for i in range(0, len(contours)):
        output_all_image = cv2.drawContours(image, contours, i, (255, 0, 0), 1)
        
    output_all_image = cv2.drawContours(output_all_image, [cnt], 0, (0, 0, 255), 1)

    cnts =  contours_image, [cnt], hierarchy

    cnts = imutils.grab_contours(cnts)
    c = max(cnts, key=cv2.contourArea)
    
    #こいつが戦犯
    extBot = tuple(c[c[:, :, 1].argmax()][0])
    #extLeft = tuple(c[c[:, :, 0].argmin()][0])
    cv2.circle(frame,extBot,15,[0,0,255],-1)  
    #cv2.circle(frame,extLeft,10,[0,255,0],-1)  
    
    #cv2.imwrite("C:\\Users\\tuzuk\\Desktop\\point_check\\" + str(count) +  ".JPG",frame)

    pointA.append(extBot)


    #凹凸検出
    hull = cv2.convexHull(cnt,returnPoints = False)
    
    defects = cv2.convexityDefects(cnt,hull)
    
    if defects is None:
        print('defects is None err in ',count)
        pointB.append(('',''))

        count = count+1
        
        continue

    '''
    #print(defects)
    print('extLeft',extLeft)
    print('extBot',extBot)
    print('hull',hull)
    print('defects',defects)
    '''
    
    i = 0
    root_list = []
    
    
    while i <= defects.shape[0]-1:
        print(i)
        s,e,f,d = defects[i,0]
        start = tuple(cnt[s][0])
        end = tuple(cnt[e][0])
        far = tuple(cnt[f][0])
    
        '''
        print("start",start)
        print("end",end)
        print("far",far)
        
        print(i)
        '''
        
        #cv2.line(image,start,end,[0,255,0],2)
        #cv2.circle(frame,far,5,[255,255,0],-1)        
        #cv2.imwrite("C:\\Users\\tuzuk\\Desktop\\point_check\\" + str(count) +  ".JPG",frame)
        
        if extBot[0]-70 <= far[0] and far[0] <= extBot[0]+70:
            #cv2.circle(frame,far,5,[255,0,255],-1)  
            root_list.append(far)
            i = i + 1
            
            
            #print("far",far)
       
       
        i = i + 1
        continue
        
    #print('root_list',len(root_list))

        
    pointB_tmp = []
    #print('len(root_list)',len(root_list))
    
    if len(root_list) == 1 :
        pointB.append(('',''))
        count = count+1

        continue
    

    elif len(root_list) == 2:
        pointB_tmp = (int((root_list[0][0] + root_list[1][0])/2),(int((root_list[0][1] + root_list[1][1])/2)))
        pointB.append(pointB_tmp)
        cv2.circle(frame,pointB_tmp,15,[255,0,0],-1)        
        cv2.imwrite("C:\\Users\\tuzuk\\Desktop\\point_check\\" + str(count) +  ".JPG",frame)
        count = count+1

        continue
    
    elif len(root_list) == 0 :
        pointB.append(('',''))
        print('pointB is 0 in',count)
        count = count+1
        
        continue
    
    
    elif len(root_list) >= 30 :
        pointB.append(('',''))
        print('pointB is over in',count)
        count = count+1
        
        continue
    
    
    else:
        line = 0
        len_pointB_before = len(pointB)
        pointB.append(('',''))
        New_cmp = 0
        Old_cmp = 0
        #print('pointB',len(pointB))
        while line < extBot[1]:
            above_line = extBot[1] - 120 -line
            under_line = extBot[1] - 60 -line
            #print(line)
            pointB_tmp = []
            for i in range(0 ,len(root_list)):
                    if root_list[i][1] <= under_line and above_line <= root_list[i][1]:
                        pointB_tmp.append(root_list[i])  
            
                        if len(pointB_tmp) == 2:
                            tmp = (int((pointB_tmp[0][0] + pointB_tmp[1][0])/2),int((pointB_tmp[0][1] + pointB_tmp[1][1])/2))  
                            cv2.circle(frame,tmp,15,[255,0,0],-1)        
                            cv2.imwrite("C:\\Users\\tuzuk\\Desktop\\point_check\\" + str(count) +  ".JPG",frame)
                            pointB.pop(-1)
                            pointB.append(tmp)
                            #print('pointB',len(pointB))
                            New_cmp =len(pointB)
            
                    
            if New_cmp > Old_cmp:
                Old_cmp = New_cmp
                break
    
            if New_cmp <= Old_cmp:
                line = line + 30
                continue
        
            #pointB_fin.append(pointB[MaxIndex])
       
        
        count = count + 1
        continue
    
        
        '''
        line = divided_height
        while line <=  extBot[1]:
            
            
            above_line = extBot[1] - first_line - line
            under_line = extBot[1] - line
            
            pointB_tmp = []
            for i in range(0 ,len(root_list)):
                if root_list[i][1] <= under_line and above_line <= root_list[i][1]:
                    
                    pointB_tmp.append(root_list[i])                    
                    if len(pointB_tmp) == 2:
                        tmp = (int((pointB_tmp[0][0] + pointB_tmp[1][0])/2),int((pointB_tmp[0][1] + pointB_tmp[1][1])/2))
                        cv2.circle(frame,tmp,10,[255,0,0],-1)        
                        cv2.imwrite("C:\\Users\\tuzuk\\Desktop\\point_check\\" + str(count) +  ".JPG",frame)
                        pointB.append(tmp)
                    
     
            
                line = line + divided_height
                continue
        '''
     
        #######################
        ###下のコードがあったとこ####
        #######################

    #print('pointB',pointB_tmp,'in', count)
        
       
         
    #cv2.circle(image,pointB,15,[255,0,0],-1)
    #cv2.circle(image,extBot,15,[0,0,255],-1)
    
    count = count+1
    
    #print(pointA)  
    
    if not ret:
        print('finish')
        break  # 映像取得に失敗
        
       

    
 
#print('pointB',pointB) 
#print('pointA',pointA)  
   
output = cv2.imread("C:\\Users\\tuzuk\\Desktop\\2.JPG")
path_A = "C:\\Users\\tuzuk\\Desktop\\pointA_cood.TXT"
path_B = "C:\\Users\\tuzuk\\Desktop\\pointB_cood.TXT"

with open(path_A,"w") as f_A:
    
    for k in range(0,len(pointA)):
        text = str(pointA[k][0]) + "," + str(pointA[k][1]) + '\n'
        f_A.write(text)
        if not pointA[k][0] == '':
            cv2.circle(output,pointA[k],5,[0,0,255],-1)
            
            #cv2.circle(output,pointA[k],5,[0,0,255],-1)
        #print(pointA[k])

     
with open(path_B,"w") as f_B:
       
    for i in range(0,len(pointB)):
        
        #print('pointB[i]',pointB[i], i)
        text = str(pointB[i][0]) + "," + str(pointB[i][1]) + '\n'
        f_B.write(text)
        if not pointB[i][0] == '':
            cv2.circle(output,pointB[i],5,[255,0,0],-1)
        #print(pointB[i])
  
#cv2.imwrite("C:\\Users\\tuzuk\\Desktop\\PointAandPointB.png",output)
cv2.imwrite("C:\\Users\\tuzuk\\Desktop\\PointAandPointB.png",output)
    
print('width',width)
print('height',height)
 


'''   
        for slide in range(0,height,divided_height):
            
            under_line = extBot[1] - slide
            above_line = extBot[1] - first_line -slide
            
            #print('under_line',under_line)
            #print('above_line',above_line)

            
            k=0
            #print(len(root_list))  
            while k <= len(root_list)-1:
            #print('root_list',root_list[k][1])
                #print('under_line_inWhile',under_line)
                #print('above_line_inWhile',above_line)
                #print('k',k)
                if root_list[k][1] <= under_line and above_line <= root_list[k][1]:
                    pointB_tmp_tmp = (root_list[k][0],root_list[k][1])
                    pointB_tmp.append(pointB_tmp_tmp)
                    k = k+1

                    continue
                    
                k = k+1
                continue       
            #print('len',len(pointB_tmp))
            if len(pointB_tmp) == 2:
                print('pointB',pointB_tmp)
                pointB.append(pointB_tmp[0])
                #print('pointB',pointB)
                        
           
        count = count+1
        continue
    '''





   

    
 