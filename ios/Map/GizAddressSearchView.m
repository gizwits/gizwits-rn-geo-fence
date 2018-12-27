//
//  GizAddressSearchView.m
//  TestGeofencing
//
//  Created by Minus🍀 on 2018/11/27.
//

#import "GizAddressSearchView.h"

@interface GizAddressSearchView () <UITextFieldDelegate>

@property (strong, nonatomic) IBOutlet UIView *contentView;
@property (weak, nonatomic) IBOutlet UITextField *textField;
@property (weak, nonatomic) IBOutlet UIButton *searchButton;

@end

@implementation GizAddressSearchView

- (void)awakeFromNib {
    [super awakeFromNib];
    
    [[NSBundle mainBundle] loadNibNamed:@"GizAddressSearchView" owner:self options:nil];
    self.contentView.frame = self.bounds;
    [self addSubview:self.contentView];
    
    self.backgroundColor = [[UIColor whiteColor] colorWithAlphaComponent:0.9];
    self.clipsToBounds = YES;
    self.layer.cornerRadius = 12;
    
    [self.searchButton setImage:[UIImage imageNamed:@"GizRegionResource.bundle/address_search"] forState:UIControlStateNormal];
    
    self.textField.delegate = self;
    [self.textField addTarget:self action:@selector(textFieldTextDidChangeEvent:) forControlEvents:UIControlEventEditingChanged];
}

- (void)layoutSubviews {
    [super layoutSubviews];
    self.contentView.frame = self.bounds;
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)setPlaceholder:(NSString *)placeholder {
    self.textField.placeholder = placeholder;
}

- (NSString *)placeholder {
    return self.textField.placeholder;
}

- (void)setText:(NSString *)text {
    self.textField.text = text;
}

- (NSString *)text {
    return self.textField.text;
}

- (void)setThemeColor:(UIColor *)themeColor {
    _themeColor = themeColor;
    
    self.searchButton.tintColor = themeColor;
}

#pragma mark - Actions

- (IBAction)actionSearch:(id)sender {
    [self.textField resignFirstResponder];
    
    if (self.delegate && [self.delegate respondsToSelector:@selector(searchView:didUpdateText:)]) {
        [self.delegate searchView:self didUpdateText:self.textField.text];
    }
}

#pragma mark - UITextFieldDelegate

- (void)textFieldDidBeginEditing:(UITextField *)textField {
    if (self.delegate && [self.delegate respondsToSelector:@selector(searchViewDidBeginEditing:)]) {
        [self.delegate searchViewDidBeginEditing:self];
    }
}

- (void)textFieldDidEndEditing:(UITextField *)textField {
    if (self.delegate && [self.delegate respondsToSelector:@selector(searchViewDidEndEditing:)]) {
        [self.delegate searchViewDidEndEditing:self];
    }
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [self actionSearch:nil];
    return YES;
}

- (void)textFieldTextDidChangeEvent:(id)sender {
    
    if (self.textField.markedTextRange) {
        return;
    }
    
    if (self.delegate && [self.delegate respondsToSelector:@selector(searchView:didUpdateText:)]) {
        [self.delegate searchView:self didUpdateText:self.textField.text];
    }
}

@end
