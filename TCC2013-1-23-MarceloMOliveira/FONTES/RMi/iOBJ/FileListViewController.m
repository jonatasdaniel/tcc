//
//  FileListViewController.m
//  RMi
//
//  Created by Marcelo da Mata.
//
//

#import "FileListViewController.h"
#import "DicomDecoder.h"

@interface FileListViewController ()

@property (nonatomic, strong) NSArray *files;

@end

@implementation FileListViewController

- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    
    if (self) {
        [self initialize];
    }
    
    return self;
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    if (self) {
        [self initialize];
    }
    
    return self;
}

- (void)initialize
{
    self.files = [[NSArray alloc] init];
    self.selectedFile = nil;
}

/*
 este metodo obtem o diretorio de documentos da aplicacao e verifica quais diretorios possuem 
 arquivos DICOM dentro deste.
 */
- (void)viewDidLoad
{
    [super viewDidLoad];
    
    NSString *documentsPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSAllDomainsMask, YES) objectAtIndex:0];
    
    NSError *error;
    
    NSArray *contentsDir = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:documentsPath error:&error];
    
    if (!error) {
        NSMutableArray *newFiles = [[NSMutableArray alloc] init];
        
        for (NSString *file in contentsDir) {
            
            NSString *extension = [file pathExtension];
            
            if ([[extension lowercaseString] isEqualToString:@"obj"]) {
                continue;
            }
            
            NSFileManager *fm = [[NSFileManager alloc] init];
            BOOL isDir;
            [fm fileExistsAtPath:[NSString stringWithFormat:documentsPath, file] isDirectory:&isDir];
            
            if (isDir) {
                NSString *newPath = [NSString stringWithFormat:@"%@/%@", documentsPath, file];
                
                NSArray *contentsDirDicom = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:newPath error:&error];
                
                BOOL hasDicom = false;
                for (NSString *fileDicom in contentsDirDicom) {
                    DicomDecoder *dc = [[DicomDecoder alloc] init:newPath :fileDicom];
                    hasDicom |= [dc isDicom];
                    if(hasDicom) {
                        [newFiles addObject:file];
                        break;
                    }
                }
            }
        }
        
        self.files = newFiles;
        
        [self.filesTableView reloadData];
    }
#ifdef DEBUG
    else {
        NSLog(@"Couldn't load resources from '%@'", documentsPath);
    }
#endif
    
}

- (void)close
{
    if (self.delegate && [self.delegate respondsToSelector:@selector(fileListWillClose:)]) {
        [self.delegate fileListWillClose:self];
    }
    
    [self dismissViewControllerAnimated:YES completion:nil];
    
    if (self.delegate && [self.delegate respondsToSelector:@selector(fileListDidClose:)]) {
        [self.delegate fileListDidClose:self];
    }
}

- (IBAction)cancel:(id)sender
{
    [self close];
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (tableView == self.filesTableView && indexPath.row < self.files.count) {
        NSString *file = [self.files objectAtIndex:indexPath.row];
        
        self.selectedFile = file;
        
        [self.filesTableView reloadData];
        
        if (self.delegate && [self.delegate respondsToSelector:@selector(fileList:selectedFile:)]) {
            [self.delegate fileList:self selectedFile:file];
        }
        
        [self close];
    }
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return self.files.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Cell"];
    
    NSString *file = [self.files objectAtIndex:indexPath.row];
    
    cell.textLabel.text = file;
    
    if (self.selectedFile && ![self.selectedFile isEqualToString:@""] && [self.selectedFile isEqualToString:file]) {
        cell.accessoryType = UITableViewCellAccessoryCheckmark;
    
    } else {
        cell.accessoryType = UITableViewCellAccessoryNone;
    }
    
    return cell;
}

@end
