# 3D Engine in Java
This is a very small engine written in plain java which renders 3D scenes.
The math behind is very simple - without any matrix transformations. Just trigonometric solutions

# Features
  - Perspective projection
  - FOV
  - Rotation/Translation for the entire scene/camera
  - Triangle/Line Clipping
  - **New** 3D Depth algorithm - Modified Painter's algo - now using a dependency graph which is topologically sorted and which generates a perfect result
  - Right click to select the visible triangle
  - **New** Added light effects
  - **New** Added culling filter and improved speed
  - **New** Added shading. This can be disabled
# Objects file format
The engine supports vertexes, normals and faces defined in standard `.obj` files. Standard [here](https://www.cs.cmu.edu/~mbz/personal/graphics/obj.html).

However other objects can be defined in `.txt` files, following another standard.
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
![image](https://user-images.githubusercontent.com/25268629/111033327-68aab980-8419-11eb-86b5-32f26277f8da.png)
![image](https://user-images.githubusercontent.com/25268629/110945484-67f52300-8346-11eb-8f87-9533e4aa0f91.png)
![image](https://user-images.githubusercontent.com/25268629/110708865-fe6cfb80-8203-11eb-934c-27f13e22536c.png)
![image](https://user-images.githubusercontent.com/25268629/110204895-2532d700-7e7e-11eb-813d-b8256f4c9d78.png)
