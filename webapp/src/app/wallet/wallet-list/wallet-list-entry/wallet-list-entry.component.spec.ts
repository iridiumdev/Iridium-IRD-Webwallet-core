import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { WalletListEntryComponent } from './wallet-list-entry.component';

describe('WalletListEntryComponent', () => {
  let component: WalletListEntryComponent;
  let fixture: ComponentFixture<WalletListEntryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WalletListEntryComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WalletListEntryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
