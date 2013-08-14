//
//  ScreenManager.m
//  RMi
//
//  Created by Marcelo da Mata.
//
//

#import "ScreenManager.h"
#import "GraphicObject.h"
#import "ProgressiveMesh.h"

/*
 Esta classe auxilia a classe Viewer3DControler na visualizacao do volume.
*/
@interface ScreenManager ()

@property (nonatomic, strong) GraphicObject *originalGraphicObject;
@property (nonatomic, strong) GraphicObject *graphicObjectWithProgressiveMesh;
@property (nonatomic) GLuint lastProgressivePercentage;

@end

@implementation ScreenManager

- (id)initWithGraphicObject:(GraphicObject *)graphicObject
{
    self = [super init];
    
    if (self) {
        self.originalGraphicObject = graphicObject;
        self.type = ScreenManagerTypeNormal;
        self.graphicObjectWithProgressiveMesh = nil;
        self.lastProgressivePercentage = 0.0f;
    }
    
    return self;
}

- (GraphicObject *)currentGraphicObject
{
    GraphicObject *graphicObject = self.originalGraphicObject;
    
    return graphicObject;
}

- (GLuint)verticesCount
{
    GLuint vertices = 0;
    
    if (self.type == ScreenManagerTypeProgressiveMesh) {
        
    } else {
        if(self.currentGraphicObject.getModeRender == AXIAL) {
            vertices = [self.currentGraphicObject.volume getPointsAxialCount];
        } else if(self.currentGraphicObject.getModeRender == SAGITAL) {
            vertices = [self.currentGraphicObject.volume getPointsSagitalCount];
        } else if(self.currentGraphicObject.getModeRender == CORONAL) {
            vertices = [self.currentGraphicObject.volume getPointsCoronalCount];
        }
    }
    
    return vertices;
}

- (void)setGraphicObject:(GraphicObject *)graphicObject {
    self.originalGraphicObject = graphicObject;
}

@end
