import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NavRefComponent } from './nav-ref.component';

describe('NavRefComponent', () => {
  let component: NavRefComponent;
  let fixture: ComponentFixture<NavRefComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NavRefComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NavRefComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
