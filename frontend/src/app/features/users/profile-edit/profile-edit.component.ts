import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-profile-edit',
  templateUrl: './profile-edit.component.html'
})
export class ProfileEditComponent implements OnInit {

  profileForm!: FormGroup;

  loading = false;
  pageLoading = true;
  saved = false;
  error = '';
  userId: number = 0;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    public router: Router,
    private userService: UserService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.userId = this.authService.getCurrentUserId() ?? 0;

    // Redirect if not own profile
    const routeId = +this.route.snapshot.params['id'];
    if (routeId && routeId !== this.userId) {
      this.router.navigate(['/profile', this.userId, 'edit']);
      return;
    }

    this.profileForm = this.fb.group({
      firstName: [''],
      lastName: [''],
      bio: [''],
      location: [''],
      website: [''],
      privacy: ['PUBLIC']
    });

    this.loadUser();
  }

  loadUser(): void {
    this.pageLoading = true;
    this.userService.getCurrentUserProfile().subscribe({
      next: res => {
        this.profileForm.patchValue(res);
        this.pageLoading = false;
      },
      error: () => { this.pageLoading = false; }
    });
  }

  onSubmit(): void {
    if (!this.profileForm) return;

    this.loading = true;
    this.saved = false;
    this.error = '';

    this.userService.updateProfile(this.profileForm.value).subscribe({
      next: () => {
        this.saved = true;
        this.loading = false;
        // Navigate back after 1.5s
        setTimeout(() => this.router.navigate(['/profile', this.userId]), 1500);
      },
      error: (err) => {
        this.error = err.error?.message || 'Update failed. Please try again.';
        this.loading = false;
      }
    });
  }
}
