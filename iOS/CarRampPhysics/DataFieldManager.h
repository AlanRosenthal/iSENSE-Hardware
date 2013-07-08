//
//  DataFieldManager.h
//  iOS Car Ramp Physics
//
//  Created by Mike Stowell on 2/21/13.
//  Copyright 2013 iSENSE Development Team. All rights reserved.
//  Engaging Computing Lab, Advisor: Fred Martin
//

#import <Foundation/Foundation.h>
#import "Fields.h"
#import "DataSaver.h"

@interface DataFieldManager : NSObject {}

- (NSMutableArray *) getFieldOrderOfExperiment:(int)exp;
- (NSMutableArray *) orderDataFromFields:(Fields *)f;

@property (nonatomic, retain) NSMutableArray *order;
@property (nonatomic, retain) NSMutableArray *data;

@end