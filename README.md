# 3D Engine in Java
This is a very small engine written in plain java which renders 3D scenes.
The math behind is very simple - without any matrix transformations. Just trigonometric solutions

# Features
  - Perspective projection
  - FOV
  - Rotation/Translation for the entire scene/camera
  - Triangle/Line Clipping
  - **New** 3D Depth algorithm - Painter's algo
# Objects file format
The objects to be rendered should be stored in `obj.txt` file.
The format is at it follows:
  - Any empty line is ignored
  - Any line starting with `/` is ignored
  - Every valid line describes a triangle followed optionally by the color
  - Every line consists of three `blocks` separated by empty spaces and representing individual point coordinates for the corner of triangle
    - `x1,y1,z1` \<space\> `x2,y2,z2` \<space\> `x3,y3,z3` \<space\> \<color\> 
  - Color format is in RGB values separated by comma:
    - `r,g,b`   
# Rendering Examples
![image](https://user-images.githubusercontent.com/25268629/110204895-2532d700-7e7e-11eb-813d-b8256f4c9d78.png)
![image](https://user-images.githubusercontent.com/25268629/110189181-f1789280-7e26-11eb-8409-ac71e8bd2d31.png)
![image](https://user-images.githubusercontent.com/25268629/110189263-2edd2000-7e27-11eb-9915-b356c18db927.png)
