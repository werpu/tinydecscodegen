import { TestBed, inject } from '@angular/core/testing';

import { RestServiceService } from './rest-service.service';

describe('RestServiceService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [RestServiceService]
    });
  });

  it('should be created', inject([RestServiceService], (service: RestServiceService) => {
    expect(service).toBeTruthy();
  }));
});
