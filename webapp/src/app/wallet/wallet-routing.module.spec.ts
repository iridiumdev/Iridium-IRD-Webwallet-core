import { WalletRoutingModule } from './wallet-routing.module';

describe('WalletRoutingModule', () => {
  let walletRoutingModule: WalletRoutingModule;

  beforeEach(() => {
    walletRoutingModule = new WalletRoutingModule();
  });

  it('should create an instance', () => {
    expect(walletRoutingModule).toBeTruthy();
  });
});
