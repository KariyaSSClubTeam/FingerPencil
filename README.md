# FingerPencil
FingerPencilとは、机に指で書いた文字を入力することを目的とするプロジェクトおよびソフトウェアの名称です。  
(別称: 机に指で書いた文字を入力するソフトウェア) (旧称: HandRecognizer)  
  
OpenCV本体、およびArUco(OpenCV_Conribに内包)を使用しています。

## ソフトウェアの構成
- FingerPencil(本体): 研究プロジェクトの本体。現在は下記のFingerPencil_QuickLoadに内包されている。
- FingerPencil_QuickLoad: GUI画面操作が可能となっているバージョン。
- MouseControle_Fileing: 生成した文字の軌跡点群を用いて、マウスポインターの操作を行うソフトウェア。
- FingerRecognizer: 実際の指先の認識を行うソフトウェア。
- GetSubstituteFingerPoint: 指先の代替物の認識を行うソフトウェア。色認識の確認用。

## 研究の参考資料
ディジタル画像処理 [改訂新版]  

# ライセンス
LGPL v3  
