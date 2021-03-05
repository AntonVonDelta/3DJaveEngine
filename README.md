# 3D Engine in Java
This is a very small engine written in plain java which renders 3D scenes.
The math behind is very simple - without any matrix transformations. Just trigonometric solutions

# Objects file format
The objects to be rendered should be stored in `obj.txt` file.
The format is at it follows:
  - Any empty line is ignored
  - Any line starting with `/` is ignored
  - Every valid line describes a triangle followed optionally by the color
  - Every line consists of three `blocks` separated by empty spaces and representing individual point coordinates for the corner of triangle
    - `x1,y1,z1` <space> `x2,y2,z2` <space> `x3,y3,z3` <space> <color> 
  - Color format is in RGB values separated by comma:
    - `r,g,b`   
