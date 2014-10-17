# Image Crop for Android

this is a image crop for android.
that can get square image.


# How to use it

1. use crop activity
```java
new Crop(Uri.fromFile(
    new File(Environment.getExternalStorageDirectory() + "/pic/jjjj.jpg")))     //the picture want to crop      
    .output(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/pic/first.jpg")))  //the file to save crop
    .withWidth(640)                         //the max width want to save
     .start(this);
```

need add the content in you AndroidManifest.xml file:

```xml
<activity android:name="me.isming.crop.CropImageActivity" />
```

can see demo in the DemoActivity.

2.use crop view

use in xml layout

```xml
<me.isming.crop.view.CropImageLayout
        android:id="@+id/clip"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

or in java class:

```java 
new CropImageLayout(context);
```

# Author

if has other question， can contact me by my weibo,blog,email:

blog： [http://blog.isming.me](http://blog.isming.me)

Weibo: [@码农明明桑](http://weibo.com/mingmingsang)

E-Mail: linming1007@gmail.com


    写英文好痛苦啊
    感觉再也不会爱了

## License
    Copyright 2014 isming.me

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.