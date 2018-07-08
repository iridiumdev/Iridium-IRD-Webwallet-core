import {Directive, Input, OnDestroy, OnInit, TemplateRef, ViewContainerRef} from '@angular/core';
import {Principal, PrincipalService} from "@elderbyte/ngx-jwt-auth";
import {NgIfContext} from "@angular/common";
import {Subscription} from "rxjs";

@Directive({
  selector: '[appIsAuthenticated]'
})
export class IsAuthenticatedDirective implements OnInit, OnDestroy {

  private context: NgIfContext = new NgIfContext();
  private principalSubscription: Subscription;

  private authRequired = true;

  constructor(
    private viewContainer: ViewContainerRef,
    private templateRef: TemplateRef<NgIfContext>,
    private principalService: PrincipalService,
  ) {
  }

  @Input()
  public set appIsAuthenticated(authRequired: boolean) {
    this.authRequired = authRequired;
    this.updateView(this.principalService.principalSnapshot);
  }

  ngOnInit(): void {
    this.principalSubscription = this.principalService.principalObservable
      .subscribe(principal => this.updateView(principal));
  }


  ngOnDestroy(): void {
    if (this.principalSubscription) {
      this.principalSubscription.unsubscribe();
      this.principalSubscription = null;
    }
  }

  private updateView(principal: Principal): void {
    this.viewContainer.clear();
    this.context.$implicit = this.context.ngIf = this.isAllowed(principal);

    if (this.context.$implicit && this.templateRef) {
      this.viewContainer.createEmbeddedView(this.templateRef, this.context);
    }
  }

  private isAllowed(principal: Principal): boolean {
    if (this.authRequired) {
      return principal && principal.isValid;
    } else {
      return principal == null;
    }

  }

}
