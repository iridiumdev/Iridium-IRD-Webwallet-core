import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { WalletOverviewComponent } from './wallet-overview.component';

describe('WalletOverviewComponent', () => {
  let component: WalletOverviewComponent;
  let fixture: ComponentFixture<WalletOverviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WalletOverviewComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WalletOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
