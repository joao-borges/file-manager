/**
 * ErrorBoundary Component
 *
 * A React error boundary that catches JavaScript errors anywhere in the child
 * component tree, logs those errors, and displays a fallback UI instead of
 * crashing the entire component tree.
 *
 * Error boundaries catch errors during:
 * - Rendering
 * - Lifecycle methods
 * - Constructors of the whole tree below them
 *
 * Error boundaries do NOT catch errors for:
 * - Event handlers (use try-catch)
 * - Asynchronous code (use try-catch)
 * - Server side rendering
 * - Errors thrown in the error boundary itself
 *
 * @see https://react.dev/reference/react/Component#catching-rendering-errors-with-an-error-boundary
 */

import { Component, type ReactNode, type ErrorInfo } from 'react';
import { Box, Button, Typography, Paper, Alert } from '@mui/material';
import { ErrorOutline as ErrorIcon } from '@mui/icons-material';

interface ErrorBoundaryProps {
  /**
   * Child components to be wrapped by the error boundary
   */
  children: ReactNode;

  /**
   * Optional fallback UI to display when an error occurs
   */
  fallback?: ReactNode;

  /**
   * Optional callback to handle errors (e.g., logging to an error reporting service)
   */
  onError?: (error: Error, errorInfo: ErrorInfo) => void;
}

interface ErrorBoundaryState {
  /**
   * Whether an error has been caught
   */
  hasError: boolean;

  /**
   * The error that was caught
   */
  error: Error | null;

  /**
   * Additional error information from React
   */
  errorInfo: ErrorInfo | null;
}

/**
 * ErrorBoundary class component
 *
 * Note: Error boundaries must be class components as React does not yet
 * support error boundaries as function components or hooks.
 */
export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
    };
  }

  /**
   * Update state so the next render will show the fallback UI
   */
  static getDerivedStateFromError(error: Error): Partial<ErrorBoundaryState> {
    return {
      hasError: true,
      error,
    };
  }

  /**
   * Log error details and call error handler
   */
  componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    // Log error to console
    console.error('ErrorBoundary caught an error:', error, errorInfo);

    // Update state with error info
    this.setState({
      errorInfo,
    });

    // Call optional error handler
    if (this.props.onError) {
      this.props.onError(error, errorInfo);
    }
  }

  /**
   * Reset error boundary state to allow retry
   */
  handleReset = (): void => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
    });
  };

  render(): ReactNode {
    if (this.state.hasError) {
      // Use custom fallback if provided
      if (this.props.fallback) {
        return this.props.fallback;
      }

      // Default fallback UI
      return (
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            minHeight: '400px',
            p: 3,
          }}
        >
          <Paper
            sx={{
              p: 4,
              maxWidth: 600,
              width: '100%',
              textAlign: 'center',
            }}
          >
            <ErrorIcon
              sx={{
                fontSize: 64,
                color: 'error.main',
                mb: 2,
              }}
            />

            <Typography variant="h5" gutterBottom>
              Something went wrong
            </Typography>

            <Typography variant="body1" color="text.secondary" paragraph>
              An unexpected error occurred. Please try again or contact support if the problem
              persists.
            </Typography>

            {this.state.error && (
              <Alert severity="error" sx={{ mt: 2, mb: 2, textAlign: 'left' }}>
                <Typography variant="subtitle2" gutterBottom>
                  Error Details:
                </Typography>
                <Typography variant="body2" component="pre" sx={{ whiteSpace: 'pre-wrap' }}>
                  {this.state.error.toString()}
                </Typography>
              </Alert>
            )}

            {process.env.NODE_ENV === 'development' && this.state.errorInfo && (
              <Alert severity="info" sx={{ mt: 2, mb: 2, textAlign: 'left' }}>
                <Typography variant="subtitle2" gutterBottom>
                  Component Stack:
                </Typography>
                <Typography
                  variant="body2"
                  component="pre"
                  sx={{
                    whiteSpace: 'pre-wrap',
                    fontSize: '0.75rem',
                    maxHeight: 200,
                    overflow: 'auto',
                  }}
                >
                  {this.state.errorInfo.componentStack}
                </Typography>
              </Alert>
            )}

            <Button variant="contained" onClick={this.handleReset} sx={{ mt: 2 }}>
              Try Again
            </Button>
          </Paper>
        </Box>
      );
    }

    // No error, render children normally
    return this.props.children;
  }
}

/**
 * Default export
 */
export default ErrorBoundary;
