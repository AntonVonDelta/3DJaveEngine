# 3D Engine in Java
This is a very small engine written in plain java which renders 3D scenes.
The math behind is very simple - without any matrix transformations. Just trigonometric solutions

# Features
  - Perspective projection
  - FOV
  - Rotation/Translation for the entire scene/camera
  - Triangle/Line Clipping
  - **NEW** 3D Depth algorithm - Modified Painter's algo - now using a dependency graph which is topologically sorted and which generates a perfect result
  - Right click to select the visible triangle
  - **NEW** Added lighting effects

# Objects file format
The objects to be rendered should be stored in `obj.txt` file.
The format is at it follows:
  - Any empty line is ignored
  - Any line starting with `/` is ignored
  - Every valid text line describes a triangle followed optionally by the color or by the debug name
      - The debug name is given to the triangle on the same line
      - Can only be given if a color is already specified a.k.a there cannot be a triangle with a debug name but not a color
  - Every line consists of three `blocks` separated by empty spaces and representing individual point coordinates for the corner of triangle
    - `x1,y1,z1` \<`space`\> `x2,y2,z2` \<`space`\> `x3,y3,z3` \<`space`\> \<`color`\> \<`debug_name`\>
  - Color format is in RGB values separated by comma:
    - `r,g,b`   

# Known Issues
  - Depth ordering inaccuracies when the triangles are intersecting or loop
  - Slow
 

# Rendering Examples
![image](https://user-images.githubusercontent.com/25268629/110861669-24a79f80-82c7-11eb-9afe-5e96a2cdd8b6.png)
![image](https://user-images.githubusercontent.com/25268629/110708865-fe6cfb80-8203-11eb-934c-27f13e22536c.png)
![image](https://user-images.githubusercontent.com/25268629/110204895-2532d700-7e7e-11eb-813d-b8256f4c9d78.png)
![image](https://user-images.githubusercontent.com/25268629/110189181-f1789280-7e26-11eb-8409-ac71e8bd2d31.png)
![image](https://user-images.githubusercontent.com/25268629/110189263-2edd2000-7e27-11eb-9915-b356c18db927.png)
