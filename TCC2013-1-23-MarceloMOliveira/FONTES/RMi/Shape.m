//
//  Slice.m
//  RMi
//
//  Created by Marcelo da Mata on 13/04/2013.
//
//

#import "Shape.h"
#import "ConstantsDicomRender.h"

float OBLIQUITY_THRESHOLD_COSINE_VALUE = 0.8f;
GLint numVertexAxial = 72;
GLint numVertexSagital = 72;
GLint numVertexCoronal = 72;
GLint numVertexTexture = 24;

/*
 Alguns valores abaixo estao comentados pois somente esta sendo visualizada uma face da fatia
 deixando assim um espaco visivel entre uma fatia e outra
 */
GLfloat textureValuesAxial[24] = {
    /*
    //3
    0.0f, 0.0f, //1
    1.0f, 1.0f, //4
    1.0f, 0.0f, //3
    
    0.0f, 0.0f, //1
    0.0f, 1.0f, //2
    1.0f, 1.0f, //4
     

    //3
    0.0f, 0.0f, //1
    1.0f, 1.0f, //4
    1.0f, 0.0f, //3
    
    0.0f, 0.0f, //1
    0.0f, 1.0f, //2
    1.0f, 1.0f, //4
     */
    
    /*
    //3
    1.0f, 1.0f, //4
    0.0f, 1.0f, //2
    0.0f, 0.0f, //1
    
    1.0f, 0.0f, //3
    1.0f, 1.0f, //4
    0.0f, 0.0f, //1
     */
    
    //5
    0.0f, 1.0f, //2
    1.0f, 1.0f, //4
    1.0f, 0.0f, //3
    
    0.0f, 1.0f, //2
    1.0f, 0.0f, //3
    0.0f, 0.0f, //1
     
    /*
    //5
    0.0f, 1.0f, //2
    1.0f, 1.0f, //4
    1.0f, 0.0f, //3
    
    0.0f, 1.0f, //2
    1.0f, 0.0f, //3
    0.0f, 0.0f, //1
     */
};

GLfloat textureValuesSagital[24] = {
     //2
     0.0f, 1.0f, //2
     1.0f, 0.0f, //3
     0.0f, 0.0f, //1
     
     0.0f, 1.0f, //2
     1.0f, 1.0f, //4
     1.0f, 0.0f, //3
    
    /*
     //4
     1.0f, 1.0f, //4
     1.0f, 0.0f, //3
     0.0f, 0.0f, //1
     
     1.0f, 1.0f, //4
     0.0f, 0.0f, //1
     0.0f, 1.0f, //2
     */
};

GLfloat textureValuesCoronal[24] = {
     //1
     1.0f, 1.0f, //4
     0.0f, 0.0f, //1
     0.0f, 1.0f, //2
     
     1.0f, 1.0f, //4
     1.0f, 0.0f, //3
     0.0f, 0.0f, //1
    
    /*
     //6
     0.0f, 1.0f, //2
     1.0f, 1.0f, //4
     1.0f, 0.0f, //3
     
     0.0f, 1.0f, //2
     1.0f, 0.0f, //3
     0.0f, 0.0f, //1
     */
};

GLfloat gCubeVertexDataAxial[72] =
{
    // Data layout for each line below is:
    // positionX, positionY, positionZ,     normalX, normalY, normalZ,
    
    /*
    //3
    0.0f, 1.0f, 0.0f,         0.0f, 1.0f, 0.0f,
    1.0f, 1.0f, 1.0f,        0.0f, 1.0f, 0.0f,
    1.0f, 1.0f, 0.0f,          0.0f, 1.0f, 0.0f,
    0.0f, 1.0f, 0.0f,          0.0f, 1.0f, 0.0f,
    0.0f, 1.0f, 1.0f,        0.0f, 1.0f, 0.0f,
    1.0f, 1.0f, 1.0f,         0.0f, 1.0f, 0.0f,
     
  
    //3
    0.0f, 1.0f, 0.0f,         0.0f, 1.0f, 0.0f,
    1.0f, 1.0f, 1.0f,        0.0f, 1.0f, 0.0f,
    1.0f, 1.0f, 0.0f,          0.0f, 1.0f, 0.0f,
    0.0f, 1.0f, 0.0f,          0.0f, 1.0f, 0.0f,
    0.0f, 1.0f, 1.0f,        0.0f, 1.0f, 0.0f,
    1.0f, 1.0f, 1.0f,         0.0f, 1.0f, 0.0f,
    */
    
    
    //5
    0.0f, 0.0f, 0.0f,          0.0f, -1.0f, 0.0f,
    1.0f, 0.0f, 0.0f,         0.0f, -1.0f, 0.0f,
    1.0f, 0.0f, 1.0f,         0.0f, -1.0f, 0.0f,
    0.0f, 0.0f, 0.0f,         0.0f, -1.0f, 0.0f,
    1.0f, 0.0f, 1.0f,         0.0f, -1.0f, 0.0f,
    0.0f, 0.0f, 1.0f,        0.0f, -1.0f, 0.0f,
    
    /*
    //5
    0.0f, 0.0f, 0.0f,          0.0f, -1.0f, 0.0f,
    1.0f, 0.0f, 0.0f,         0.0f, -1.0f, 0.0f,
    1.0f, 0.0f, 1.0f,         0.0f, -1.0f, 0.0f,
    0.0f, 0.0f, 0.0f,         0.0f, -1.0f, 0.0f,
    1.0f, 0.0f, 1.0f,         0.0f, -1.0f, 0.0f,
    0.0f, 0.0f, 1.0f,        0.0f, -1.0f, 0.0f,
     */
    
};

GLfloat gCubeVertexDataSagital[72] =
{
    // Data layout for each line below is:
    // positionX, positionY, positionZ,     normalX, normalY, normalZ,
     
     //2
     0.0f, 0.0f, 0.0f,        -1.0f, 0.0f, 0.0f,
     0.0f, 1.0f, 1.0f,       -1.0f, 0.0f, 0.0f,
     0.0f, 1.0f, 0.0f,         -1.0f, 0.0f, 0.0f,
     0.0f, 0.0f, 0.0f,         -1.0f, 0.0f, 0.0f,
     0.0f, 0.0f, 1.0f,       -1.0f, 0.0f, 0.0f,
     0.0f, 1.0f, 1.0f,        -1.0f, 0.0f, 0.0f,
    
    /*
     //4
     1.0f, 0.0f, 0.0f,       1.0f, 0.0f, 0.0f,
     1.0f, 1.0f, 0.0f,        1.0f, 0.0f, 0.0f,
     1.0f, 1.0f, 1.0f,        1.0f, 0.0f, 0.0f,
     1.0f, 0.0f, 0.0f,        1.0f, 0.0f, 0.0f,
     1.0f, 1.0f, 1.0f,        1.0f, 0.0f, 0.0f,
     1.0f, 0.0f, 1.0f,         1.0f, 0.0f, 0.0f,
     */
    
};

GLfloat gCubeVertexDataCoronal[72] =
{
    // Data layout for each line below is:
    // positionX, positionY, positionZ,     normalX, normalY, normalZ,
    
     //1
     0.0f, 0.0f, 0.0f,        0.0f, 0.0f, -1.0f,
     1.0f, 1.0f, 0.0f,         0.0f, 0.0f, -1.0f,
     1.0f, 0.0f, 0.0f,         0.0f, 0.0f, -1.0f,
     0.0f, 0.0f, 0.0f,         0.0f, 0.0f, -1.0f,
     0.0f, 1.0f, 0.0f,          0.0f, 0.0f, -1.0f,
     1.0f, 1.0f, 0.0f,         0.0f, 0.0f, -1.0f,
    
    /*
     //6
     0.0f, 0.0f, 1.0f,        0.0f, 0.0f, 1.0f,
     1.0f, 0.0f, 1.0f,       0.0f, 0.0f, 1.0f,
     1.0f, 1.0f, 1.0f,         0.0f, 0.0f, 1.0f,
     0.0f, 0.0f, 1.0f,         0.0f, 0.0f, 1.0f,
     1.0f, 1.0f, 1.0f,       0.0f, 0.0f, 1.0f,
     0.0f, 1.0f, 1.0f,        0.0f, 0.0f, 1.0f
     */
};



@implementation Shape

- (id)init
{
    self = [super init];
 
    
    if (self) {
        _faces = [[NSMutableArray alloc] init];
        self.pointsSlice = [[NSMutableArray alloc] init];
        self.pointsTextureLenght = 0;
        direction = SLICE_ORIENTATION_AXIAL;
        n = 0;
    }
    
    return self;
}

-(id) initWithImage:(UIImage *)image {
    self = [self init];
    
    if(self) {
        self.imageTexture = image;
    }
    
    return self;
}


-(void)setOrientation:(float)x1 :(float)y1 :(float)z1 :(float)x2 :(float)y2 :(float)z2 {
    orientation[0] = x1; orientation[1] = y1; orientation[2] = z1;
    orientation[3] = x2; orientation[4] = y2; orientation[5] = z2;
}

-(int)getOrientation {
    return direction;
}

-(void)setOrientation: (GLint) o {
    direction = o;
}

-(int) calculateOrientation {
    char rowAxis = [self getAxisDirectionCosine:orientation[0] :orientation[1] :orientation[2]];
    char colAxis = [self getAxisDirectionCosine:orientation[3] :orientation[4] :orientation[5]];
    
    if((rowAxis == 'R' || rowAxis == 'L') && (colAxis == 'A' || colAxis == 'P'))
        direction =  SLICE_ORIENTATION_AXIAL;
    else if((colAxis == 'R' || colAxis == 'L') && (rowAxis == 'A' || rowAxis == 'P'))
        direction =  SLICE_ORIENTATION_AXIAL;
    else if((rowAxis == 'R' || rowAxis == 'L') && (colAxis == 'H' || colAxis == 'F'))
        direction = SLICE_ORIENTATION_CORONAL;
    else if((colAxis == 'R' || colAxis == 'L') && (rowAxis == 'A' || rowAxis == 'P'))
        direction = SLICE_ORIENTATION_CORONAL;
    else if((rowAxis == 'A' || rowAxis == 'P') && (colAxis == 'H' || colAxis == 'F'))
        direction = SLICE_ORIENTATION_SAGITAL;
    else if((colAxis == 'A' || colAxis == 'P') && (rowAxis == 'H' || rowAxis == 'F'))
        direction = SLICE_ORIENTATION_SAGITAL;
    //else
        //este caso nao esta sendo tratado
        //direction = OBLIQUE
        
    return direction;
}

-(char)getAxisDirectionCosine: (float) x :(float) y :(float) z {
    char axis = 0;
    
    /*
     verifica a orientacao nos 3 eixos
     */
    char orientationX = x < 0 ? 'R' : 'L';
    char orientationY = y < 0 ? 'A' : 'P';
    char orientationZ = z < 0 ? 'F' : 'H';
    
    float absX = fabs(x);
    float absY = fabs(y);
    float absZ = fabs(z);
    
    if(absX > OBLIQUITY_THRESHOLD_COSINE_VALUE && absX > absY && absX > absZ) {
        axis = orientationX;
    } else if(absY > OBLIQUITY_THRESHOLD_COSINE_VALUE && absY > absX && absY > absZ) {
        axis = orientationY;
    } else if(absZ > OBLIQUITY_THRESHOLD_COSINE_VALUE && absZ > absX && absZ > absY) {
        axis = orientationZ;
    }
    
    return axis;
}

-(void)setSizes:(float)widthNum :(float)heightNum :(float)depthNum {
    width = widthNum;
    height = heightNum;
    depth = depthNum;
}

-(float)getWidth {
    return width;
}

-(float)getHeight {
    return height;
}

-(float)getDepth {
    return depth;
}

- (void)generatePoints: (BOOL)points
{
    /*
     Os pontos criados abaixo sao necessarios para posicionar a camera corretamente posteriormente.
     Sao criados os 8 pontos do hexaedro que representa uma fatia.
     */
    if(points) {
        //x +/-
        for(int i = 0; i < 2; i++) {
            //y +/-
            for(int j = 0; j < 2; j++) {
                //z +/-
                for(int k = 0; k < 2; k++) {
                    GLfloat x = i == 1? width * 1.0f * 0:width * 1.0f;
                    GLfloat y = j == 1? height * 1.0f * 0:height * 1.0f;
                    GLfloat z = k == 1? depth * 1.0f * 0:depth * 1.0f;
                    GLKVector3 pointSlice = GLKVector3Make(x, y, z);
                    NSValue *value = [NSValue value:&pointSlice withObjCType:@encode(GLKVector3)];
                    [self.pointsSlice addObject:value];
                }
            }
        }
    }
    
    int vertexSize;
    GLfloat *gCubeVertexDataDirection;
    
    /*
     Seleciona os arrays com cordenadas dos pontos e normais
     */
    if(direction == SLICE_ORIENTATION_AXIAL) {
        vertexSize = numVertexAxial;
        gCubeVertexDataDirection = gCubeVertexDataAxial;
    } else if(direction == SLICE_ORIENTATION_SAGITAL) {
        vertexSize = numVertexSagital;
        gCubeVertexDataDirection = gCubeVertexDataSagital;
    } else {
        vertexSize = numVertexCoronal;
        gCubeVertexDataDirection = gCubeVertexDataCoronal;
    }
    
    /*
     Atraves da orientacao da fatia realiza a translacao dos pontos conforme o tamanho da imagem 
     e o espaco entre as fatias.
     */
    Face3 *face[12];
    for (int i=0, l = 0, t = 0; i<vertexSize; t+=6) {
        face[l] = [[Face3 alloc] init];
        Vertex *vertex[3];
        for (int j=0; j<3; j++) {
            GLKVector3 point[3];
            GLKVector3 normal[3];
            vertex[j] = [[Vertex alloc] init];
            for (int k=0; k<3; k++) {
                point[j].v[k] = gCubeVertexDataDirection[i++];
            }
            for (int k=0; k<3; k++) {
                normal[j].v[k] = gCubeVertexDataDirection[i++];
            }
            
            if(direction == SLICE_ORIENTATION_AXIAL) {
                point[j].v[0] *= width;
                point[j].v[1] *= thickness;
                point[j].v[2] *= depth;
                point[j].v[1] += height;
            } else if(direction == SLICE_ORIENTATION_SAGITAL) {
                point[j].v[0] *= thickness;
                point[j].v[1] *= height;
                point[j].v[2] *= depth;
                point[j].v[0] += width;
            } else {
                point[j].v[0] *= width;
                point[j].v[1] *= height;
                point[j].v[2] *= thickness;
                point[j].v[2] += depth;
            }
            
            vertex[j].point = point[j];
            vertex[j].normal = normal[j];
        }
        face[l].vertices = [NSMutableArray arrayWithObjects:vertex[0], vertex[1], vertex[2], nil];
        face[l].material = [self getMaterial];
        face[l].textures = [self addTextures:t];
        
        [self addFace:face[l]];
        l++;
    }
    
    self.material = [self getMaterial];
}

- (NSMutableArray *)addTextures: (int)num
{
    NSMutableArray *textures = [[NSMutableArray alloc] init];
    GLfloat *textureValues;
    
    if(direction == SLICE_ORIENTATION_AXIAL) {
        textureValues = textureValuesAxial;
    } else if(direction == SLICE_ORIENTATION_SAGITAL) {
        textureValues = textureValuesSagital;
    } else {
        textureValues = textureValuesCoronal;
    }
    
    for (int i = 0; i < 6; i+=2) {
        GLKVector2 textureCoordinate = GLKVector2Make(textureValues[num+i], textureValues[num+i+1]);
        NSValue *value = [NSValue value:&textureCoordinate withObjCType:@encode(GLKVector2)];
        [textures addObject:value];
    }
    textureValues = nil;
    return textures;
}


-(Material*)getMaterial {
    NSString *name = [NSString stringWithFormat:@"%d", n];
    //num deve ser a quantidade de texturas cadastradas
    Material *currentMaterial = [[Material alloc] initWithName:name];
    
    GLKVector4 color = GLKVector4Make(0.0f, 0.0f, 0.0f, 1.0f);
    currentMaterial.specularColor = color;
    
    GLKVector4 diffuseColor = GLKVector4Make(0.5f, 0.5f, 0.5f, 1.0f);
    currentMaterial.diffuseColor = diffuseColor;
    
    GLKVector4 ambientColor = GLKVector4Make(0.0f, 0.0f, 0.0f, 1.0f);
    currentMaterial.ambientColor = ambientColor;
    
    n++;
    return currentMaterial;
}


-(NSMutableArray *)getPoints {
    return self.pointsSlice;
}

- (void)addFace:(Face3 *)face
{
    [self.faces addObject:face];
    
    [self addTrianglesWithFace:face];
}

-(BOOL)haveColors {
    return haveColors;
}

-(float)getThickness {
    return thickness;
}

-(void)setThickness:(float)t {
    thickness = t;
}

-(void)setDepth: (float) d {
    depth = d;
}

-(id)copy {
    Shape *slice = [[Shape alloc] initWithImage:nil];
    
    return slice;
}

@end
