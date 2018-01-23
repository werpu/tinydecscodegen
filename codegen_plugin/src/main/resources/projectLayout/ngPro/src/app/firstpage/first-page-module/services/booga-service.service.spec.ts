import { TestBed, inject } from '@angular/core/testing';

import { BoogaServiceService } from './booga-service.service';

describe('BoogaServiceService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [BoogaServiceService]
    });
  });

  it('should be created', inject([BoogaServiceService], (service: BoogaServiceService) => {
    expect(service).toBeTruthy();
  }));
});
